package com.tidewater.freight.rate;

import com.tidewater.freight.exception.UnknownZoneException;
import com.tidewater.freight.model.ZoneRate;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Map-backed {@link RateRepository}. Default rates are loaded from
 * {@code src/main/resources/rates.properties}; callers may also supply a
 * custom map for tests or alternative configurations.
 *
 * <p>The map is defensively copied so callers cannot mutate the repository
 * after construction.
 *
 * <p>To add a zone, add one line to {@code rates.properties} — no Java class
 * changes (open-closed principle, §12.2).
 */
public class InMemoryRateRepository implements RateRepository {

    private static final String DEFAULT_PROPERTIES = "/rates.properties";

    private final Map<String, ZoneRate> rates;

    /**
     * Constructs a repository from a caller-supplied map.
     *
     * @param rates map of zone identifier to {@link ZoneRate}; defensively copied
     */
    public InMemoryRateRepository(Map<String, ZoneRate> rates) {
        if (rates == null || rates.isEmpty()) {
            throw new IllegalArgumentException("rates map must not be null or empty");
        }
        this.rates = Map.copyOf(rates);
    }

    /**
     * Loads the default rates from {@code rates.properties} on the classpath.
     *
     * @return a repository pre-loaded with Tidewater's current zone rates
     */
    public static InMemoryRateRepository withDefaultRates() {
        Properties props = new Properties();
        try (InputStream in = InMemoryRateRepository.class
                .getResourceAsStream(DEFAULT_PROPERTIES)) {
            if (in == null) {
                throw new IllegalStateException(
                        "rates.properties not found on classpath");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not load rates.properties", e);
        }

        Map<String, ZoneRate> map = new HashMap<>();
        for (String zone : props.stringPropertyNames()) {
            String value = props.getProperty(zone).trim();
            String[] parts = value.split(",");
            if (parts.length != 2) {
                throw new IllegalStateException(
                        "rates.properties: zone '" + zone
                        + "' must have format 'ratePerKg, flatFee', got: " + value);
            }
            try {
                BigDecimal rate = new BigDecimal(parts[0].trim());
                BigDecimal fee = new BigDecimal(parts[1].trim());
                map.put(zone, new ZoneRate(rate, fee));
            } catch (NumberFormatException e) {
                throw new IllegalStateException(
                        "rates.properties: zone '" + zone
                        + "' has malformed numeric value: " + value, e);
            }
        }
        return new InMemoryRateRepository(map);
    }

    @Override
    public ZoneRate findRate(String zone) {
        ZoneRate rate = rates.get(zone);
        if (rate == null) {
            throw new UnknownZoneException(zone);
        }
        return rate;
    }
}
