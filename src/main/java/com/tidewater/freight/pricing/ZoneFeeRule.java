package com.tidewater.freight.pricing;

import com.tidewater.freight.model.ShipmentRequest;
import com.tidewater.freight.model.ZoneRate;

import java.math.BigDecimal;

/**
 * Rule 2 — zone flat fee: adds {@link ZoneRate#flatFee()} to the running cost.
 *
 * <p>The fee lives in {@link ZoneRate} alongside the rate, so a zone cannot
 * exist in the repository without its fee. This eliminates the split-rules
 * defect in the original code.
 */
public class ZoneFeeRule implements PricingRule {

    @Override
    public BigDecimal apply(BigDecimal currentCost,
                            ShipmentRequest request,
                            ZoneRate zoneRate) {
        return currentCost.add(zoneRate.flatFee());
    }
}
