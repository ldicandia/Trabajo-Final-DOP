package org.example.domain;

import java.time.Instant;

public record TrafficEvent(
        String id,
        Instant timestamp,
        double speedKmh,
        int lane
) implements UnifiedEvent {
}
