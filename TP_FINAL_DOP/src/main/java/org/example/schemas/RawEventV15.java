package org.example.schemas;

import java.time.Instant;

public record RawEventV15(
        String id,
        Instant timestamp,
        Double version,
        String kind,
        Double velocity,
        Double temp_c,
        String category,
        Attributes attributes
) implements RawEvent {
    public record Attributes(
            String lane_id,
            Double HUMIDITY,
            String severity,
            String area
    ) {}
}
