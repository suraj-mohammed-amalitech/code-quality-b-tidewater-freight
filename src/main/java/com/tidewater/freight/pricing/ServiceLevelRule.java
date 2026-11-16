package com.tidewater.freight.pricing;

import com.tidewater.freight.model.ServiceLevel;
import com.tidewater.freight.model.ShipmentRequest;
import com.tidewater.freight.model.ZoneRate;

import java.math.BigDecimal;

/**
 * Rule 4 — express multiplier: multiplies the accumulated cost by 1.5 for
 * EXPRESS shipments.
 *
 * <p>This rule <strong>must be last</strong> in the pipeline. The business
 * rule is that the express multiplier applies to everything accumulated so
 * far — base price, zone fee, and heavy surcharge included.
 *
 * <p>The multiplier is constructor-injected so it can be tested and changed
 * without editing this class.
 */
public class ServiceLevelRule implements PricingRule {

    private final BigDecimal expressMultiplier;

    public ServiceLevelRule(BigDecimal expressMultiplier) {
        this.expressMultiplier = expressMultiplier;
    }

    @Override
    public BigDecimal apply(BigDecimal currentCost,
                            ShipmentRequest request,
                            ZoneRate zoneRate) {
        if (request.serviceLevel() == ServiceLevel.EXPRESS) {
            return currentCost.multiply(expressMultiplier);
        }
        return currentCost;
    }
}
