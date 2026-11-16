package com.tidewater.freight.exception;

/**
 * Base for every domain failure the shipping service can produce.
 *
 * <p>Callers that only need to distinguish "pricing failed" from "succeeded"
 * catch this type. Callers that need to react to a specific failure (bad zone,
 * bad input, unavailable repository) catch the appropriate subtype.
 *
 * <p>All subclasses are unchecked: a failed quote is a data or availability
 * problem the immediate caller cannot retry around, so checked exceptions
 * would only add {@code throws} noise on every method (§6.1).
 */
public abstract class ShippingException extends RuntimeException {

    protected ShippingException(String message) {
        super(message);
    }

    protected ShippingException(String message, Throwable cause) {
        super(message, cause);
    }
}
