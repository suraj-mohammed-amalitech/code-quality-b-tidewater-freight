package com.tidewater.freight.pricing;

import com.tidewater.freight.model.ServiceLevel;
import com.tidewater.freight.model.ShipmentRequest;
import com.tidewater.freight.model.ZoneRate;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ZoneFeeRuleTest {

    private final PricingRule rule = new ZoneFeeRule();

    @Test
    void addsFlatFeeToCurrentCost() {
        ZoneRate rate     = new ZoneRate(new BigDecimal("8.00"), new BigDecimal("10.00"));
        ShipmentRequest r = new ShipmentRequest("REGIONAL", 5.0, ServiceLevel.STANDARD);

        BigDecimal result = rule.apply(new BigDecimal("40.00"), r, rate);

        assertEquals(new BigDecimal("50.00"), result);
    }

    @Test
    void zeroFeeLeavesCostUnchanged() {
        ZoneRate rate     = new ZoneRate(new BigDecimal("5.00"), BigDecimal.ZERO);
        ShipmentRequest r = new ShipmentRequest("DOMESTIC", 10.0, ServiceLevel.STANDARD);

        BigDecimal result = rule.apply(new BigDecimal("50.00"), r, rate);

        assertEquals(new BigDecimal("50.00"), result);
    }
}
