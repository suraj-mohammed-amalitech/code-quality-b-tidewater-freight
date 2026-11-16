package com.tidewater.freight.rate;

import com.tidewater.freight.exception.UnknownZoneException;
import com.tidewater.freight.model.ZoneRate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryRateRepositoryTest {

    private final InMemoryRateRepository repository =
            InMemoryRateRepository.withDefaultRates();

    @ParameterizedTest(name = "zone={0} ratePerKg={1} flatFee={2}")
    @CsvSource({
        "DOMESTIC,       5.00,  0.00",
        "REGIONAL,       8.00, 10.00",
        "INTERNATIONAL, 15.00, 25.00"
    })
    void defaultRatesAreCorrect(String zone, String rate, String fee) {
        ZoneRate zoneRate = repository.findRate(zone);
        assertEquals(new BigDecimal(rate), zoneRate.ratePerKg());
        assertEquals(new BigDecimal(fee),  zoneRate.flatFee());
    }

    @Test
    void unknownZoneThrowsWithZoneName() {
        UnknownZoneException ex = assertThrows(UnknownZoneException.class,
                () -> repository.findRate("MOON"));
        assertEquals("MOON", ex.zone());
        assertTrue(ex.getMessage().contains("MOON"));
    }

    @Test
    void customRatesCanBeSupplied() {
        InMemoryRateRepository custom = new InMemoryRateRepository(Map.of(
                "ARCTIC", new ZoneRate(new BigDecimal("20.00"), new BigDecimal("40.00"))
        ));
        ZoneRate rate = custom.findRate("ARCTIC");
        assertEquals(new BigDecimal("20.00"), rate.ratePerKg());
        assertEquals(new BigDecimal("40.00"), rate.flatFee());
    }

    @Test
    void ratesMapIsDefensiveCopy() {
        Map<String, ZoneRate> mutable = new HashMap<>();
        mutable.put("DOMESTIC", new ZoneRate(new BigDecimal("5.00"), BigDecimal.ZERO));

        InMemoryRateRepository repo = new InMemoryRateRepository(mutable);
        mutable.put("DOMESTIC", new ZoneRate(new BigDecimal("99.00"), BigDecimal.ZERO));

        assertEquals(new BigDecimal("5.00"), repo.findRate("DOMESTIC").ratePerKg());
    }

    @Test
    void nullMapThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new InMemoryRateRepository(null));
    }

    @Test
    void emptyMapThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new InMemoryRateRepository(Map.of()));
    }
}
