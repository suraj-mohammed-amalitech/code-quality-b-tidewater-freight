package com.tidewater.freight.model;

import com.tidewater.freight.exception.InvalidShipmentException;

/**
 * The two service levels Tidewater offers.
 *
 * <p>Using an enum instead of a free string means typos such as {@code "express"}
 * or {@code "OVERNIGHT"} are caught at compile time (or at the
 * {@link #fromText(String)} boundary) rather than silently priced as STANDARD.
 */
public enum ServiceLevel {

    STANDARD,
    EXPRESS;

    /**
     * Parses a service-level name supplied as text (e.g. from a CSV or HTTP
     * request). Matching is case-insensitive.
     *
     * @param text the raw text
     * @return the matching {@code ServiceLevel}
     * @throws InvalidShipmentException if the text does not match any level
     */
    public static ServiceLevel fromText(String text) {
        if (text == null) {
            throw new InvalidShipmentException("Service level must not be null");
        }
        try {
            return ServiceLevel.valueOf(text.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidShipmentException(
                    "Unknown service level: \"" + text + "\" — expected STANDARD or EXPRESS");
        }
    }
}
