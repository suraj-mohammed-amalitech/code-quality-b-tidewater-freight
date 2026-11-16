package com.tidewater.freight.pricing;

import java.math.BigDecimal;
import java.util.List;

/**
 * Assembles the standard Tidewater pricing pipeline in the correct order.
 *
 * <p>Order matters: {@link ServiceLevelRule} must be last because the express
 * multiplier applies to the accumulated total, including zone fee and heavy
 * surcharge.
 *
 * <p>Business constants live here and are package-visible for tests.
 */
public final class DefaultPricingRules {

    static final BigDecimal HEAVY_THRESHOLD_KG = new BigDecimal("30");
    static final BigDecimal HEAVY_SURCHARGE    = new BigDecimal("20.00");
    static final BigDecimal EXPRESS_MULTIPLIER = new BigDecimal("1.5");

    private DefaultPricingRules() {
        // utility class — not instantiated
    }

    /**
     * Returns an unmodifiable list of pricing rules in pipeline order.
     *
     * <ol>
     *   <li>{@link BasePriceRule} — rate × weight</li>
     *   <li>{@link ZoneFeeRule} — flat zone handling fee</li>
     *   <li>{@link HeavyItemSurchargeRule} — surcharge for weight &gt; 30 kg</li>
     *   <li>{@link ServiceLevelRule} — express ×1.5 (must be last)</li>
     * </ol>
     */
    public static List<PricingRule> create() {
        return List.of(
                new BasePriceRule(),
                new ZoneFeeRule(),
                new HeavyItemSurchargeRule(HEAVY_THRESHOLD_KG, HEAVY_SURCHARGE),
                new ServiceLevelRule(EXPRESS_MULTIPLIER)
        );
    }
}
