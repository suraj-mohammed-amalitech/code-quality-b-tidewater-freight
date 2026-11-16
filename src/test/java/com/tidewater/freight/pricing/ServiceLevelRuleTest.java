package com.tidewater.freight.pricing;

import com.tidewater.freight.model.ServiceLevel;
import com.tidewater.freight.model.ShipmentRequest;
import com.tidewater.freight.model.ZoneRate;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ServiceLevelRuleTest {

    private static final BigDecimal MULTIPLIER = new BigDecimal("1.5");
    private static final ZoneRate   ANY_RATE   =
            new ZoneRate(new BigDecimal("5.00"), BigDecimal.ZERO);

    private final PricingRule rule = new ServiceLevelRule(MULTIPLIER);

    @Test
    void expressMultipliesAccumulatedCost() {
        ShipmentRequest r = new ShipmentRequest("DOMESTIC", 10.0, ServiceLevel.EXPRESS);

        BigDecimal result = rule.apply(new BigDecimal("50.00"), r, ANY_RATE);

        assertEquals(new BigDecimal("75.000"), result);
    }

    @Test
    void standardLeavesAccumulatedCostUnchanged() {
        ShipmentRequest r = new ShipmentRequest("DOMESTIC", 10.0, ServiceLevel.STANDARD);

        BigDecimal result = rule.apply(new BigDecimal("50.00"), r, ANY_RATE);

        assertEquals(new BigDecimal("50.00"), result);
    }

    @Test
    void expressMultipliesFeesAndSurchargeAlso() {
        // 600 base + 25 fee + 20 surcharge = 645; express = 645 * 1.5 = 967.5
        ShipmentRequest r = new ShipmentRequest("INTERNATIONAL", 40.0, ServiceLevel.EXPRESS);

        BigDecimal result = rule.apply(new BigDecimal("645.00"), r, ANY_RATE);

        assertEquals(0, result.compareTo(new BigDecimal("967.50")));
    }
}
