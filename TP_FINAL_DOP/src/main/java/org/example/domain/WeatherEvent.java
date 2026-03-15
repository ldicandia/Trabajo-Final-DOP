package org.example.domain;

import java.time.Instant;
import java.util.Objects;

public record WeatherEvent(
        String id,
        Instant timestamp,
        double temperatureC,
        Double humidity
) implements UnifiedEvent {
    public WeatherEvent {
        Objects.requireNonNull(id);
        Objects.requireNonNull(timestamp);
    }
}
