package com.tidewater.freight.pricing;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultPricingRulesTest {

    @Test
    void createReturnsFourRulesInCorrectOrder() {
        List<PricingRule> rules = DefaultPricingRules.create();
        
        assertEquals(4, rules.size(), "Should have exactly 4 rules");
        assertInstanceOf(BasePriceRule.class, rules.get(0),
                "First rule must be BasePriceRule");
        assertInstanceOf(ZoneFeeRule.class, rules.get(1),
                "Second rule must be ZoneFeeRule");
        assertInstanceOf(HeavyItemSurchargeRule.class, rules.get(2),
                "Third rule must be HeavyItemSurchargeRule");
        assertInstanceOf(ServiceLevelRule.class, rules.get(3),
                "Fourth rule must be ServiceLevelRule — express multiplies all prior fees");
    }

    @Test
    void listIsUnmodifiable() {
        List<PricingRule> rules = DefaultPricingRules.create();
        assertThrows(UnsupportedOperationException.class,
                () -> rules.add(new BasePriceRule()));
    }
}
