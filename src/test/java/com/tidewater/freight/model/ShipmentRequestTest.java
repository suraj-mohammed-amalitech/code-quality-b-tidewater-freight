package com.tidewater.freight.model;

import com.tidewater.freight.exception.InvalidShipmentException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ShipmentRequestTest {

    @Test
    void validRequestIsConstructed() {
        ShipmentRequest r = new ShipmentRequest("DOMESTIC", 10.0, ServiceLevel.STANDARD);
        assertEquals("DOMESTIC", r.zone());
        assertEquals(new BigDecimal("10.0"), r.weightKg());
        assertEquals(ServiceLevel.STANDARD, r.serviceLevel());
    }

    @Test
    void nullZoneThrows() {
        InvalidShipmentException ex = assertThrows(InvalidShipmentException.class,
                () -> new ShipmentRequest(null, 10.0, ServiceLevel.STANDARD));
        assertTrue(ex.getMessage().toLowerCase().contains("zone"));
    }

    @Test
    void blankZoneThrows() {
        assertThrows(InvalidShipmentException.class,
                () -> new ShipmentRequest("   ", 10.0, ServiceLevel.STANDARD));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, -0.001})
    void nonPositiveWeightThrows(double weight) {
        assertThrows(InvalidShipmentException.class,
                () -> new ShipmentRequest("DOMESTIC", weight, ServiceLevel.STANDARD));
    }

    @Test
    void nullServiceLevelThrows() {
        InvalidShipmentException ex = assertThrows(InvalidShipmentException.class,
                () -> new ShipmentRequest("DOMESTIC", 10.0, null));
        assertTrue(ex.getMessage().toLowerCase().contains("service level"));
    }

    @Test
    void tinyPositiveWeightIsAccepted() {
        assertDoesNotThrow(
                () -> new ShipmentRequest("DOMESTIC", 0.001, ServiceLevel.STANDARD));
    }

    @Test
    void bigDecimalConstructorWorks() {
        ShipmentRequest r = new ShipmentRequest(
                "REGIONAL", new BigDecimal("2.3"), ServiceLevel.EXPRESS);
        assertEquals(new BigDecimal("2.3"), r.weightKg());
    }

    @Test
    void nullBigDecimalWeightThrows() {
        assertThrows(InvalidShipmentException.class,
                () -> new ShipmentRequest("DOMESTIC", (BigDecimal) null, ServiceLevel.STANDARD));
    }
}
