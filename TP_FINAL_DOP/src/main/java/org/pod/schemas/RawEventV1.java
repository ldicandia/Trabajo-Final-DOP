package org.pod.schemas;

import java.time.Instant;

public record RawEventV1(
        String id,
        Instant timestamp,
        String SCHEMA_VER,
        String TYPE,
        Payload PAYLOAD
) implements RawEvent {
    public record Payload(
            Double SPD,
            Integer LNE,
            Double T,
            Double H,
            String CAT,
            String DESC
    ) {}
}
