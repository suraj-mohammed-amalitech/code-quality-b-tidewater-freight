package com.tidewater.freight;

import java.math.BigDecimal;

import com.tidewater.freight.exception.ShippingException;
import com.tidewater.freight.model.ServiceLevel;
import com.tidewater.freight.model.ShipmentRequest;
import com.tidewater.freight.rate.InMemoryRateRepository;

/**
 * Console entry point — demonstrates valid quotes and domain error reporting.
 *
 * <p>This class holds no business logic; it is excluded from coverage and
 * mutation gates.
 */
public class Demo {

    public static void main(String[] args) {
        ShippingCalculator calculator =
                ShippingCalculator.withDefaultRules(InMemoryRateRepository.withDefaultRates());

        quote(calculator, "DOMESTIC",      10,  ServiceLevel.STANDARD);
        quote(calculator, "INTERNATIONAL", 40,  ServiceLevel.EXPRESS);
        quote(calculator, "MOON",          10,  ServiceLevel.STANDARD);
    }

    private static void quote(ShippingCalculator calc,
                               String zone, double weight, ServiceLevel level) {
        try {
            ShipmentRequest request = new ShipmentRequest(zone, weight, level);
            BigDecimal cost = calc.calculateCost(request);
            System.out.println("Result " + zone + " " + weight + "kg "
                    + level + " -> GHS " + cost);
        } catch (ShippingException e) {
            System.out.println("Result " + zone + " " + weight + "kg "
                    + level + " -> cannot price shipment: " + e.getMessage());
        }
    }
}
