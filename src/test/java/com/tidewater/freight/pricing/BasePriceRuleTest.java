package com.tidewater.freight.pricing;

import com.tidewater.freight.model.ServiceLevel;
import com.tidewater.freight.model.ShipmentRequest;
import com.tidewater.freight.model.ZoneRate;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BasePriceRuleTest {

    private final PricingRule rule = new BasePriceRule();

    @Test
    void addsRateTimesWeightToCurrentCost() {
        ZoneRate rate     = new ZoneRate(new BigDecimal("5.00"), BigDecimal.ZERO);
        ShipmentRequest r = new ShipmentRequest("DOMESTIC", 10.0, ServiceLevel.STANDARD);

        BigDecimal result = rule.apply(BigDecimal.ZERO, r, rate);

        assertEquals(0, result.compareTo(new BigDecimal("50.00")));
    }

    @Test
    void accumulatesOnTopOfExistingCost() {
        ZoneRate rate     = new ZoneRate(new BigDecimal("5.00"), BigDecimal.ZERO);
        ShipmentRequest r = new ShipmentRequest("DOMESTIC", 10.0, ServiceLevel.STANDARD);

        BigDecimal result = rule.apply(new BigDecimal("100"), r, rate);

        // 100 + 50 = 150
        assertEquals(0, result.compareTo(new BigDecimal("150.00")));
    }

    @Test
    void fractionalWeightIsExact() {
        ZoneRate rate     = new ZoneRate(new BigDecimal("8.00"), BigDecimal.ZERO);
        ShipmentRequest r = new ShipmentRequest("REGIONAL",
                new BigDecimal("2.3"), ServiceLevel.STANDARD);

        BigDecimal result = rule.apply(BigDecimal.ZERO, r, rate);

        assertEquals(0, result.compareTo(new BigDecimal("18.40")));
    }
}
