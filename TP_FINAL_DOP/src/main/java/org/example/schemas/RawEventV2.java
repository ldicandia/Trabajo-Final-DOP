package org.example.schemas;

import java.time.Instant;

public record RawEventV2(
        String schemaVersion,
        String id,
        Instant timestamp,
        String eventType,
        Data data
) implements RawEvent {
    public record Data(
            Double speedKmh,
            Integer lane,
            Double temperature,
            Double humidity,
            String category,
            String status
    ) {}
}
