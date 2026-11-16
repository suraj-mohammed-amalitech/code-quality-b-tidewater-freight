package com.tidewater.freight;

import com.tidewater.freight.exception.RateUnavailableException;
import com.tidewater.freight.exception.UnknownZoneException;
import com.tidewater.freight.model.ServiceLevel;
import com.tidewater.freight.model.ShipmentRequest;
import com.tidewater.freight.model.ZoneRate;
import com.tidewater.freight.pricing.DefaultPricingRules;
import com.tidewater.freight.rate.RateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ShippingCalculator}.
 *
 * The repository is mocked so each test controls exactly what rates are
 * returned; every assertion is on the computed {@link BigDecimal} result.
 */
@ExtendWith(MockitoExtension.class)
class ShippingCalculatorTest {

    @Mock RateRepository repository;

    ShippingCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new ShippingCalculator(repository, DefaultPricingRules.create());
    }

    // -------------------------------------------------------------------------
    // Happy-path calculations
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Happy-path calculations")
    class HappyPath {

        @ParameterizedTest(name = "{0} {1}kg {2} = GHS {3}")
        @CsvSource({
            "DOMESTIC,       10,  STANDARD,  50.00",
            "REGIONAL,        5,  STANDARD,  50.00",
            "INTERNATIONAL,  40,  EXPRESS,  967.50",
            "DOMESTIC,       31,  STANDARD, 175.00",
            "DOMESTIC,       30,  STANDARD, 150.00",
            "DOMESTIC,       10,  EXPRESS,   75.00"
        })
        void costTableMatchesReferenceValues(
                String zone, double weight, String level, String expected) {
            ZoneRate rate = rateFor(zone);
            when(repository.findRate(zone)).thenReturn(rate);

            ShipmentRequest request = new ShipmentRequest(
                    zone, weight, ServiceLevel.fromText(level));
            BigDecimal cost = calculator.calculateCost(request);

            assertEquals(new BigDecimal(expected), cost);
        }

        @Test
        @DisplayName("result is always rounded to exactly two decimal places")
        void resultRoundedToTwoDecimalPlaces() {
            when(repository.findRate("DOMESTIC"))
                    .thenReturn(new ZoneRate(new BigDecimal("3.33"), BigDecimal.ZERO));

            ShipmentRequest r = new ShipmentRequest("DOMESTIC", 1.0, ServiceLevel.STANDARD);
            BigDecimal cost = calculator.calculateCost(r);

            assertEquals(2, cost.scale());
        }

        @Test
        @DisplayName("express multiplier is applied after fees and surcharge, not before")
        void ruleOrderMattersExpressMustComeAfterFee() {
            // DOMESTIC: rate=5, fee=0 — 10 kg express
            // correct:  (5*10) * 1.5 = 75.00
            // wrong order (express first): 5*10*1.5 + 0 = 75 (same here, so use fee zone)
            // REGIONAL: rate=8, fee=10 — 5 kg express
            // correct:  (8*5 + 10) * 1.5 = 70.00  NOT (8*5*1.5 + 10) = 70 either
            // Use INTERNATIONAL 1 kg express: (15*1 + 25)*1.5 = 60.00
            // If express applied before fee: 15*1*1.5 + 25 = 47.50 — clearly different
            when(repository.findRate("INTERNATIONAL"))
                    .thenReturn(new ZoneRate(new BigDecimal("15.00"), new BigDecimal("25.00")));

            ShipmentRequest r = new ShipmentRequest("INTERNATIONAL", 1.0, ServiceLevel.EXPRESS);
            BigDecimal cost = calculator.calculateCost(r);

            assertEquals(new BigDecimal("60.00"), cost,
                    "Express must multiply base + fee, not just base");
        }

        @Test
        @DisplayName("fractional weight produces exact BigDecimal result")
        void fractionalWeightIsExact() {
            // REGIONAL 2.3 kg STANDARD: 8 * 2.3 + 10 = 18.4 + 10 = 28.40
            when(repository.findRate("REGIONAL"))
                    .thenReturn(new ZoneRate(new BigDecimal("8.00"), new BigDecimal("10.00")));

            ShipmentRequest r = new ShipmentRequest(
                    "REGIONAL", new BigDecimal("2.3"), ServiceLevel.STANDARD);
            BigDecimal cost = calculator.calculateCost(r);

            assertEquals(new BigDecimal("28.40"), cost);
        }
    }

    // -------------------------------------------------------------------------
    // Failure handling
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Failure handling")
    class FailureHandling {

        @Test
        @DisplayName("unknown zone propagates UnknownZoneException with zone name")
        void unknownZonePropagates() {
            when(repository.findRate("MOON")).thenThrow(new UnknownZoneException("MOON"));

            ShipmentRequest r = new ShipmentRequest("MOON", 10.0, ServiceLevel.STANDARD);
            UnknownZoneException ex = assertThrows(UnknownZoneException.class,
                    () -> calculator.calculateCost(r));

            assertEquals("MOON", ex.zone());
        }

        @Test
        @DisplayName("UnknownZoneException is a ShippingException — hierarchy correct")
        void unknownZoneIsAShippingException() {
            when(repository.findRate("MOON")).thenThrow(new UnknownZoneException("MOON"));

            ShipmentRequest r = new ShipmentRequest("MOON", 10.0, ServiceLevel.STANDARD);
            assertThrows(com.tidewater.freight.exception.ShippingException.class,
                    () -> calculator.calculateCost(r));
        }

        @Test
        @DisplayName("unexpected repository failure is wrapped in RateUnavailableException with cause")
        void repositoryRuntimeExceptionWrapped() {
            when(repository.findRate("DOMESTIC"))
                    .thenThrow(new RuntimeException("DB down"));

            ShipmentRequest r = new ShipmentRequest("DOMESTIC", 10.0, ServiceLevel.STANDARD);
            RateUnavailableException ex = assertThrows(RateUnavailableException.class,
                    () -> calculator.calculateCost(r));

            assertNotNull(ex.getCause());
            assertEquals("DB down", ex.getCause().getMessage());
        }

        @Test
        @DisplayName("failure is never silently returned as 0 — the original bug")
        void failureIsNeverSwallowedAsZero() {
            when(repository.findRate("DOMESTIC")).thenThrow(new RuntimeException("oops"));

            ShipmentRequest r = new ShipmentRequest("DOMESTIC", 10.0, ServiceLevel.STANDARD);
            assertThrows(com.tidewater.freight.exception.ShippingException.class,
                    () -> calculator.calculateCost(r));
        }
    }

    // -------------------------------------------------------------------------
    // Constructor guards
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Constructor guards")
    class ConstructorGuards {

        @Test
        void nullRepositoryThrows() {
            assertThrows(NullPointerException.class,
                    () -> new ShippingCalculator(null, DefaultPricingRules.create()));
        }

        @Test
        void nullRulesThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> new ShippingCalculator(repository, null));
        }

        @Test
        void emptyRulesThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> new ShippingCalculator(repository, List.of()));
        }
    }

    // -------------------------------------------------------------------------
    // New zone proof — ARCTIC zone works without any code changes
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("New zone — ARCTIC")
    class NewZone {

        @ParameterizedTest(name = "ARCTIC {0}kg {1} = GHS {2}")
        @CsvSource({
            "10,  STANDARD, 240.00",   // 20*10 + 40 = 240
            "30,  STANDARD, 640.00",   // 20*30 + 40 = 640, no surcharge at 30
            "35,  EXPRESS, 1140.00"    // (20*35 + 40 + 20) * 1.5 = 760 * 1.5 = 1140
        })
        void arcticZonePricesCorrectly(double weight, String level, String expected) {
            when(repository.findRate("ARCTIC"))
                    .thenReturn(new ZoneRate(new BigDecimal("20.00"), new BigDecimal("40.00")));

            ShipmentRequest r = new ShipmentRequest(
                    "ARCTIC", weight, ServiceLevel.fromText(level));
            BigDecimal cost = calculator.calculateCost(r);

            assertEquals(new BigDecimal(expected), cost,
                    "Adding ARCTIC zone must require no changes to ShippingCalculator");
        }
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private ZoneRate rateFor(String zone) {
        return switch (zone) {
            case "DOMESTIC"      -> new ZoneRate(new BigDecimal("5.00"),  BigDecimal.ZERO);
            case "REGIONAL"      -> new ZoneRate(new BigDecimal("8.00"),  new BigDecimal("10.00"));
            case "INTERNATIONAL" -> new ZoneRate(new BigDecimal("15.00"), new BigDecimal("25.00"));
            default -> throw new UnknownZoneException(zone);
        };
    }
}
