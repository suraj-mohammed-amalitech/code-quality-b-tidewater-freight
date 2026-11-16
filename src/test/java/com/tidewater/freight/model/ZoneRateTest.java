package com.tidewater.freight.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ZoneRateTest {

    @Test
    void constructsWithValidValues() {
        ZoneRate rate = new ZoneRate(new BigDecimal("5.00"), new BigDecimal("10.00"));
        assertEquals(new BigDecimal("5.00"), rate.ratePerKg());
        assertEquals(new BigDecimal("10.00"), rate.flatFee());
    }

    @Test
    void zeroFlatFeeIsAllowed() {
        ZoneRate rate = new ZoneRate(new BigDecimal("5.00"), BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, rate.flatFee());
    }

    @Test
    void zeroRatePerKgIsAllowed() {
        // A zone with no per-kg charge (flat fee only) is valid
        ZoneRate rate = new ZoneRate(BigDecimal.ZERO, new BigDecimal("10.00"));
        assertEquals(BigDecimal.ZERO, rate.ratePerKg());
    }

    @Test
    void toStringContainsRateAndFee() {
        ZoneRate rate = new ZoneRate(new BigDecimal("5.00"), new BigDecimal("10.00"));
        String s = rate.toString();
        assertTrue(s.contains("5.00"));
        assertTrue(s.contains("10.00"));
    }

    @Test
    void negativeRateThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new ZoneRate(new BigDecimal("-1"), BigDecimal.ZERO));
    }

    @Test
    void negativeFeeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new ZoneRate(new BigDecimal("5.00"), new BigDecimal("-0.01")));
    }

    @Test
    void nullRateThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new ZoneRate(null, BigDecimal.ZERO));
    }

    @Test
    void nullFeeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new ZoneRate(new BigDecimal("5.00"), null));
    }
}
