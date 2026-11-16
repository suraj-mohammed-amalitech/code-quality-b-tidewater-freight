package com.tidewater.freight.pricing;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultPricingRulesTest {

    @Test
    void createReturnsFourRules() {
        List<PricingRule> rules = DefaultPricingRules.create();
        assertEquals(4, rules.size());
    }

    @Test
    void firstRuleIsBasePriceRule() {
        assertInstanceOf(BasePriceRule.class, DefaultPricingRules.create().get(0));
    }

    @Test
    void secondRuleIsZoneFeeRule() {
        assertInstanceOf(ZoneFeeRule.class, DefaultPricingRules.create().get(1));
    }

    @Test
    void thirdRuleIsHeavyItemSurchargeRule() {
        assertInstanceOf(HeavyItemSurchargeRule.class, DefaultPricingRules.create().get(2));
    }

    @Test
    void lastRuleIsServiceLevelRule() {
        List<PricingRule> rules = DefaultPricingRules.create();
        assertInstanceOf(ServiceLevelRule.class, rules.get(rules.size() - 1),
                "ServiceLevelRule must be last — express multiplies all prior fees");
    }

    @Test
    void listIsUnmodifiable() {
        List<PricingRule> rules = DefaultPricingRules.create();
        assertThrows(UnsupportedOperationException.class,
                () -> rules.add(new BasePriceRule()));
    }
}
