package org.pod.domain;

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
        if (speedKmh < 0 || speedKmh > 500) {
            throw new IllegalArgumentException("Invalid speed amount");
        }
    }
}
