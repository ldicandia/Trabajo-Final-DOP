package org.pod.schemas;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;


public record RawEventV1(
        String id,
        Instant timestamp,
        @JsonProperty("SCHEMA_VER") String schemaVersion,
        @JsonProperty("TYPE") String type,
        @JsonProperty("PAYLOAD") Payload payload
) implements RawEvent {
    public record Payload(
            @JsonProperty("SPD") Double speed,
            @JsonProperty("LNE") Integer lane,
            @JsonProperty("T") Double temperature,
            @JsonProperty("H") Double humidity,
            @JsonProperty("CAT") String category,
            @JsonProperty("DESC") String description
    ) {}

}
