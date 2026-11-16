package com.tidewater.freight.model;

import com.tidewater.freight.exception.InvalidShipmentException;

import java.math.BigDecimal;

/**
 * An immutable, self-validating shipment request.
 *
 * <p>Validation happens once, here, on construction. The calculator receives a
 * {@code ShipmentRequest} and never sees invalid data: no null check, no
 * negative-weight guard scattered through pricing logic.
 *
 * <p>Weight is stored as {@link BigDecimal} so pricing rules multiply without
 * binary floating-point conversion.
 */
public final class ShipmentRequest {

    private final String       zone;
    private final BigDecimal   weightKg;
    private final ServiceLevel serviceLevel;

    /**
     * Constructs a validated shipment request.
     *
     * @param zone         destination zone — must not be null or blank
     * @param weightKg     shipment weight in kg — must be a positive finite value
     * @param serviceLevel the requested service level — must not be null
     * @throws InvalidShipmentException if any argument is invalid
     */
    public ShipmentRequest(String zone, double weightKg, ServiceLevel serviceLevel) {
        this(zone, BigDecimal.valueOf(weightKg), serviceLevel);
    }

    /**
     * Constructs a validated shipment request with a {@link BigDecimal} weight.
     *
     * @param zone         destination zone — must not be null or blank
     * @param weightKg     shipment weight in kg — must be positive
     * @param serviceLevel the requested service level — must not be null
     * @throws InvalidShipmentException if any argument is invalid
     */
    public ShipmentRequest(String zone, BigDecimal weightKg, ServiceLevel serviceLevel) {
        if (zone == null || zone.isBlank()) {
            throw new InvalidShipmentException(
                    "Zone must not be null or blank, got: \"" + zone + "\"");
        }
        if (weightKg == null || weightKg.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidShipmentException(
                    "Weight must be positive, got: " + weightKg);
        }
        if (serviceLevel == null) {
            throw new InvalidShipmentException("Service level must not be null");
        }
        this.zone         = zone;
        this.weightKg     = weightKg;
        this.serviceLevel = serviceLevel;
    }

    public String zone() {
        return zone;
    }

    public BigDecimal weightKg() {
        return weightKg;
    }

    public ServiceLevel serviceLevel() {
        return serviceLevel;
    }

    @Override
    public String toString() {
        return "ShipmentRequest{zone='" + zone + "', weightKg=" + weightKg
                + ", serviceLevel=" + serviceLevel + "}";
    }
}
