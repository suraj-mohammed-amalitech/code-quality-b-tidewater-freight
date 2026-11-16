package com.tidewater.freight.exception;

/**
 * Thrown when the rate repository fails for a reason other than an unknown
 * zone — for example a database connection lost or a configuration file
 * that cannot be read.
 *
 * <p>Carries the original cause when one exists, so the application boundary
 * can log the full stack trace once with request context (§6.3). When the
 * repository returns null (no cause available), the cause is omitted.
 */
public class RateUnavailableException extends ShippingException {

    public RateUnavailableException(String message) {
        super(message);
    }

    public RateUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
