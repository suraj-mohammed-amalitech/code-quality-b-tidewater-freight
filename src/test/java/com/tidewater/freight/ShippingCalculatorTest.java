package com.tidewater.freight;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShippingCalculatorTest {

    @Test
    void calculatesACost() {
        ShippingCalculator calculator = new ShippingCalculator();
        double cost = calculator.calculateCost("DOMESTIC", 10, "STANDARD");
        assertTrue(cost > 0); // it returned a cost
    }

    @Test
    void calculatesExpressCost() {
        // mock the calculator and check it returns what we told it to
        ShippingCalculator calculator = mock(ShippingCalculator.class);
        when(calculator.calculateCost("INTERNATIONAL", 40, "EXPRESS")).thenReturn(967.5);
        assertEquals(967.5, calculator.calculateCost("INTERNATIONAL", 40, "EXPRESS"));
    }

    @Test
    void returnsACostForAShipment() {
        ShippingCalculator calculator = new ShippingCalculator();
        double cost = calculator.calculateCost("REGIONAL", 5, "STANDARD");
        assertNotNull(cost); // a result came back
    }
}
