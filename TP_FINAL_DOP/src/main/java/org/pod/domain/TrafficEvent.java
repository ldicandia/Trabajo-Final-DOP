package org.pod.domain;

import org.pod.Constants;

import java.time.Instant;
import java.util.Objects;

public record TrafficEvent(
        String id,
        Instant timestamp,
        double speedKmh,
        int lane
) implements UnifiedEvent {
    public TrafficEvent {
        Objects.requireNonNull(id);
        Objects.requireNonNull(timestamp);
        if (speedKmh < Constants.MIN_VALID_SPEED_KMH || speedKmh > Constants.MAX_VALID_SPEED_KMH) {
            throw new IllegalArgumentException("Invalid speed amount");
        }
    }
}
