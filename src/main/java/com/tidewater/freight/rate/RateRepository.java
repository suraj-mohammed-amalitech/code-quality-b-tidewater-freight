package com.tidewater.freight.rate;

import com.tidewater.freight.exception.UnknownZoneException;
import com.tidewater.freight.model.ZoneRate;

/**
 * Port for zone-rate lookup.
 *
 * <p>The calculator depends on this interface, never on a concrete class
 * (dependency-inversion principle, §12.5). Implementations may load rates
 * from memory, a database, or a configuration file.
 */
public interface RateRepository {

    /**
     * Returns the {@link ZoneRate} configured for {@code zone}.
     *
     * @param zone the destination zone identifier
     * @return the zone's rate and flat fee — never {@code null}
     * @throws UnknownZoneException if no rate is configured for the zone
     */
    ZoneRate findRate(String zone);
}
