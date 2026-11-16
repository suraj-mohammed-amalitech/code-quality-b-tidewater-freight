package com.tidewater.freight.exception;

/**
 * Thrown when a {@link com.tidewater.freight.model.ShipmentRequest} is
 * constructed with invalid data: a blank zone, a null service level, or a
 * non-positive weight.
 *
 * <p>The message names the offending field and value so the caller can report
 * it immediately without unwrapping.
 */
public class InvalidShipmentException extends ShippingException {

    public InvalidShipmentException(String message) {
        super(message);
    }
}
