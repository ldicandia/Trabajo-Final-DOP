package org.pod.schemas;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;


public record RawEventV15(
        String id,
        Instant timestamp,
        @JsonProperty("version") Double version,
        @JsonProperty("kind") String kind,
        @JsonProperty("velocity") Double velocity,
        @JsonProperty("temp_c") Double temperatureC,
        @JsonProperty("category") String category,
        @JsonProperty("attributes") Attributes attributes
) implements RawEvent {
    public record Attributes(
            @JsonProperty("lane_id") String laneId,
            @JsonProperty("HUMIDITY") Double humidity,
            @JsonProperty("severity") String severity,
            @JsonProperty("area") String area
    ) {
        // Convenience accessor for backward compatibility
        public Double HUMIDITY() { return humidity; }
        public String lane_id() { return laneId; }
    }
    
    // Convenience accessors for backward compatibility
    public Double temp_c() { return temperatureC; }
}

