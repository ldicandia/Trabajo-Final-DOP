package org.example.domain;

import java.time.Instant;

public record WeatherEvent(
        String id,
        Instant timestamp,
        double temperatureC,
        Double humidity
) implements UnifiedEvent {
}
