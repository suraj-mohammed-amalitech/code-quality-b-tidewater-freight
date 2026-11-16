package com.tidewater.freight.pricing;

import com.tidewater.freight.model.ServiceLevel;
import com.tidewater.freight.model.ShipmentRequest;
import com.tidewater.freight.model.ZoneRate;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class HeavyItemSurchargeRuleTest {

    private static final BigDecimal THRESHOLD  = new BigDecimal("30");
    private static final BigDecimal SURCHARGE  = new BigDecimal("20.00");
    private static final ZoneRate   ANY_RATE   =
            new ZoneRate(new BigDecimal("5.00"), BigDecimal.ZERO);

    private final PricingRule rule =
            new HeavyItemSurchargeRule(THRESHOLD, SURCHARGE);

    @Test
    void surchargeAppliedWhenWeightExceedsThreshold() {
        ShipmentRequest r = new ShipmentRequest("DOMESTIC", 31.0, ServiceLevel.STANDARD);

        BigDecimal result = rule.apply(new BigDecimal("155.00"), r, ANY_RATE);

        assertEquals(new BigDecimal("175.00"), result);
    }

    @Test
    void surchargeNotAppliedAtExactlyThreshold() {
        ShipmentRequest r = new ShipmentRequest("DOMESTIC", 30.0, ServiceLevel.STANDARD);

        BigDecimal result = rule.apply(new BigDecimal("150.00"), r, ANY_RATE);

        assertEquals(new BigDecimal("150.00"), result);
    }

    @Test
    void surchargeNotAppliedBelowThreshold() {
        ShipmentRequest r = new ShipmentRequest("DOMESTIC", 29.9, ServiceLevel.STANDARD);

        BigDecimal result = rule.apply(new BigDecimal("149.50"), r, ANY_RATE);

        assertEquals(new BigDecimal("149.50"), result);
    }

    @Test
    void surchargeAppliedJustOverThreshold() {
        ShipmentRequest r = new ShipmentRequest("DOMESTIC",
                new BigDecimal("30.1"), ServiceLevel.STANDARD);

        BigDecimal result = rule.apply(new BigDecimal("150.50"), r, ANY_RATE);

        assertEquals(new BigDecimal("170.50"), result);
    }
}
