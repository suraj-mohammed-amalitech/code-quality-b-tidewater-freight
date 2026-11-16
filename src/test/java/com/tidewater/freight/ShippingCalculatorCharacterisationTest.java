package com.tidewater.freight;

import com.tidewater.freight.model.ServiceLevel;
import com.tidewater.freight.model.ShipmentRequest;
import com.tidewater.freight.rate.InMemoryRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterisation tests: pin the exact prices produced by the real
 * {@code rates.properties} file. Any refactor that moves a price fails here.
 *
 * <p>These tests run against the real {@link InMemoryRateRepository} — no mocks.
 * They were written first, against the starter code's prices, so a refactor
 * cannot accidentally change a price without a failing test.
 */
@DisplayName("Characterisation — exact prices from rates.properties")
class ShippingCalculatorCharacterisationTest {

    ShippingCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = ShippingCalculator.withDefaultRules(
                InMemoryRateRepository.withDefaultRates());
    }

    @ParameterizedTest(name = "{0} {1}kg {2} -> GHS {3}")
    @CsvSource({
        // domestic — no zone fee
        "DOMESTIC,       10,  STANDARD,  50.00",
        "DOMESTIC,       30,  STANDARD, 150.00",   // boundary: no surcharge at exactly 30
        "DOMESTIC,       31,  STANDARD, 175.00",   // boundary: surcharge at 31
        "DOMESTIC,       10,  EXPRESS,   75.00",

        // regional — GHS 10 flat fee
        "REGIONAL,        5,  STANDARD,  50.00",
        "REGIONAL,       31,  STANDARD, 278.00",   // 8*31 + 10 + 20 = 278

        // international — GHS 25 flat fee
        "INTERNATIONAL,  40,  EXPRESS,  967.50",   // (15*40 + 25 + 20) * 1.5 = 967.50
        "INTERNATIONAL,   1,  STANDARD,  40.00",   // 15*1 + 25 = 40
        "INTERNATIONAL,  31,  EXPRESS,  765.00",   // (15*31 + 25 + 20)*1.5 = 510*1.5 = 765

        // rounding
        "DOMESTIC,        1,  STANDARD,   5.00"
    })
    void pricesMatchReferenceValues(
            String zone, double weight, String level, String expected) {
        ShipmentRequest request = new ShipmentRequest(
                zone, weight, ServiceLevel.fromText(level));

        BigDecimal cost = calculator.calculateCost(request);

        assertEquals(new BigDecimal(expected), cost);
    }
}
