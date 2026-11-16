package com.tidewater.freight.model;

import java.math.BigDecimal;

/**
 * The rate configuration for a single shipping zone: a per-kilogram base rate
 * and a flat handling fee.
 *
 * <p>Keeping rate and fee together means a zone cannot exist in the repository
 * without its fee — the split-rules defect in the original code (rate in one
 * class, fee in another) is structurally impossible here.
 *
 * <p>All fields are final; instances are immutable after construction.
 */
public final class ZoneRate {

    private final BigDecimal ratePerKg;
    private final BigDecimal flatFee;

    public ZoneRate(BigDecimal ratePerKg, BigDecimal flatFee) {
        if (ratePerKg == null || ratePerKg.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "ratePerKg must be non-negative, got: " + ratePerKg);
        }
        if (flatFee == null || flatFee.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "flatFee must be non-negative, got: " + flatFee);
        }
        this.ratePerKg = ratePerKg;
        this.flatFee = flatFee;
    }

    public BigDecimal ratePerKg() {
        return ratePerKg;
    }

    public BigDecimal flatFee() {
        return flatFee;
    }

    @Override
    public String toString() {
        return "ZoneRate{ratePerKg=" + ratePerKg + ", flatFee=" + flatFee + "}";
    }
}
