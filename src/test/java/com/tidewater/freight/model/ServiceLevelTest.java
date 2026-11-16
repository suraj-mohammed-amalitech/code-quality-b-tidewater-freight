package com.tidewater.freight.model;

import com.tidewater.freight.exception.InvalidShipmentException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ServiceLevelTest {

    @Test
    void fromTextStandardCaseInsensitive() {
        assertEquals(ServiceLevel.STANDARD, ServiceLevel.fromText("standard"));
        assertEquals(ServiceLevel.STANDARD, ServiceLevel.fromText("STANDARD"));
        assertEquals(ServiceLevel.STANDARD, ServiceLevel.fromText("Standard"));
    }

    @Test
    void fromTextExpressCaseInsensitive() {
        assertEquals(ServiceLevel.EXPRESS, ServiceLevel.fromText("express"));
        assertEquals(ServiceLevel.EXPRESS, ServiceLevel.fromText("EXPRESS"));
    }

    @Test
    void fromTextTrimsWhitespace() {
        assertEquals(ServiceLevel.EXPRESS, ServiceLevel.fromText("  EXPRESS  "));
    }

    @Test
    void fromTextNullThrows() {
        InvalidShipmentException ex = assertThrows(
                InvalidShipmentException.class, () -> ServiceLevel.fromText(null));
        assertTrue(ex.getMessage().toLowerCase().contains("null"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"OVERNIGHT", "FAST", "", "express_plus"})
    void fromTextUnknownThrows(String text) {
        assertThrows(InvalidShipmentException.class,
                () -> ServiceLevel.fromText(text));
    }
}
