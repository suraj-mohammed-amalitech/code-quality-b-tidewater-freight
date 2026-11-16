package com.tidewater.freight;

import com.tidewater.freight.exception.RateUnavailableException;
import com.tidewater.freight.exception.ShippingException;
import com.tidewater.freight.exception.UnknownZoneException;
import com.tidewater.freight.model.ShipmentRequest;
import com.tidewater.freight.model.ZoneRate;
import com.tidewater.freight.pricing.DefaultPricingRules;
import com.tidewater.freight.pricing.PricingRule;
import com.tidewater.freight.rate.RateRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * Calculates the total shipping cost for a single {@link ShipmentRequest}.
 *
 * <p>This class has one job: orchestrate the pricing pipeline. It does not
 * know what the rates are, it does not know the fee rules, and it does not
 * validate input — {@link ShipmentRequest} handles that before data reaches
 * here.
 *
 * <p>All collaborators are injected (dependency-inversion principle, §12.5).
 * Use {@link #withDefaultRules(RateRepository)} to build an instance wired
 * with Tidewater's standard rule set.
 */
public class ShippingCalculator {

    private final RateRepository    rateRepository;
    private final List<PricingRule> rules;

    /**
     * Full constructor — supply any repository and any rule list.
     *
     * @param rateRepository the rate source; must not be null
     * @param rules          the ordered pricing pipeline; must not be null or empty
     */
    public ShippingCalculator(RateRepository rateRepository, List<PricingRule> rules) {
        Objects.requireNonNull(rateRepository, "rateRepository must not be null");
        if (rules == null || rules.isEmpty()) {
            throw new IllegalArgumentException("rules must not be null or empty");
        }
        this.rateRepository = rateRepository;
        this.rules = List.copyOf(rules);
    }

    /**
     * Convenience factory: wires the calculator with Tidewater's default
     * pricing rules (base price → zone fee → heavy surcharge → express ×1.5).
     *
     * @param rateRepository the rate source
     * @return a ready-to-use calculator
     */
    public static ShippingCalculator withDefaultRules(RateRepository rateRepository) {
        return new ShippingCalculator(rateRepository, DefaultPricingRules.create());
    }

    /**
     * Returns the total shipping cost for the given request, rounded to two
     * decimal places with {@link RoundingMode#HALF_UP}.
     *
     * @param request the validated shipment request
     * @return total cost in GHS, rounded to two decimal places
     * @throws UnknownZoneException    if the zone is not configured
     * @throws RateUnavailableException if the repository fails unexpectedly
     */
    public BigDecimal calculateCost(ShipmentRequest request) {
        ZoneRate zoneRate = lookUpRate(request.zone());

        BigDecimal cost = BigDecimal.ZERO;
        for (PricingRule rule : rules) {
            cost = rule.apply(cost, request, zoneRate);
        }

        return cost.setScale(2, RoundingMode.HALF_UP);
    }

    // -------------------------------------------------------------------------

    private ZoneRate lookUpRate(String zone) {
        try {
            ZoneRate rate = rateRepository.findRate(zone);
            if (rate == null) {
                throw new RateUnavailableException(
                        "Repository returned null for zone: " + zone, null);
            }
            return rate;
        } catch (UnknownZoneException e) {
            throw e;                        // propagate as-is — it carries the zone name
        } catch (ShippingException e) {
            throw e;                        // already a domain exception — pass through
        } catch (Exception e) {
            throw new RateUnavailableException(
                    "Could not retrieve rate for zone: " + zone, e);
        }
    }
}
