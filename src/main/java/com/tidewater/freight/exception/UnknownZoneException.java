package com.tidewater.freight.exception;

/**
 * Thrown when a zone name has no configured rate.
 *
 * <p>Carries the zone name so the caller or log can report exactly which zone
 * was rejected (§6.3).
 */
public class UnknownZoneException extends ShippingException {

    private final String zone;

    public UnknownZoneException(String zone) {
        super("Unknown zone: " + zone);
        this.zone = zone;
    }

    public String zone() {
        return zone;
    }
}
