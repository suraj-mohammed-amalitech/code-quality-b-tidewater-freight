package com.tidewater.freight.pricing;

import com.tidewater.freight.model.ShipmentRequest;
import com.tidewater.freight.model.ZoneRate;

import java.math.BigDecimal;

/**
 * Rule 1 — base price: {@code ratePerKg × weightKg}.
 *
 * <p>Always the first rule in the pipeline. All subsequent rules add to or
 * multiply this result.
 */
public class BasePriceRule implements PricingRule {

    @Override
    public BigDecimal apply(BigDecimal currentCost,
                            ShipmentRequest request,
                            ZoneRate zoneRate) {
        return currentCost.add(zoneRate.ratePerKg().multiply(request.weightKg()));
    }
}
