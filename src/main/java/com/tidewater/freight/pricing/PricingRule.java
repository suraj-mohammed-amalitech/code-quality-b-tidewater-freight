package com.tidewater.freight.pricing;

import com.tidewater.freight.model.ShipmentRequest;
import com.tidewater.freight.model.ZoneRate;

import java.math.BigDecimal;

/**
 * A single step in the pricing pipeline.
 *
 * <p>Each implementation does exactly one job (§4.2, §12.1). The pipeline
 * in {@link DefaultPricingRules} assembles them in the correct order. Adding
 * a new surcharge means a new class, with no edits to existing rules.
 */
public interface PricingRule {

    /**
     * Applies this rule to the running cost and returns the new total.
     *
     * @param currentCost the cost accumulated by all previous rules
     * @param request     the shipment being priced
     * @param zoneRate    the rate configuration for the shipment's zone
     * @return the updated cost
     */
    BigDecimal apply(BigDecimal currentCost, ShipmentRequest request, ZoneRate zoneRate);
}
