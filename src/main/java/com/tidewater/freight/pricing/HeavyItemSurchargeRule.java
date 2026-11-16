package com.tidewater.freight.pricing;

import com.tidewater.freight.model.ShipmentRequest;
import com.tidewater.freight.model.ZoneRate;

import java.math.BigDecimal;

/**
 * Rule 3 — heavy-item surcharge: adds a flat amount when the shipment exceeds
 * the weight threshold.
 *
 * <p>Both the threshold and the surcharge amount are constructor-injected so
 * they can be changed without editing this class. {@link DefaultPricingRules}
 * supplies the business values (30 kg, GHS 20).
 *
 * <p>Boundary: a shipment of exactly {@code thresholdKg} kg does <em>not</em>
 * attract the surcharge (strict {@code >} comparison).
 */
public class HeavyItemSurchargeRule implements PricingRule {

    private final BigDecimal thresholdKg;
    private final BigDecimal surcharge;

    public HeavyItemSurchargeRule(BigDecimal thresholdKg, BigDecimal surcharge) {
        this.thresholdKg = thresholdKg;
        this.surcharge   = surcharge;
    }

    @Override
    public BigDecimal apply(BigDecimal currentCost,
                            ShipmentRequest request,
                            ZoneRate zoneRate) {
        if (request.weightKg().compareTo(thresholdKg) > 0) {
            return currentCost.add(surcharge);
        }
        return currentCost;
    }
}
