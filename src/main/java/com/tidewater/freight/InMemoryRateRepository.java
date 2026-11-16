package com.tidewater.freight;

import java.util.HashMap;
import java.util.Map;

public class InMemoryRateRepository implements RateRepository {

    private final Map<String, Double> rates = new HashMap<>();

    public InMemoryRateRepository() {
        rates.put("DOMESTIC", 5.0);
        rates.put("REGIONAL", 8.0);
        rates.put("INTERNATIONAL", 15.0);
    }

    @Override
    public double findRate(String zone) {
        Double rate = rates.get(zone);
        if (rate == null) {
            throw new IllegalArgumentException("Unknown zone: " + zone);
        }
        return rate;
    }
}
