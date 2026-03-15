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
        if (temperatureC < -90 || temperatureC > 60) {
            throw new IllegalArgumentException("Temperature out of range: " + temperatureC);
        }
        if (humidity != null && (humidity < 0 || humidity > 100)) {
            throw new IllegalArgumentException("Humidity out of range: " + humidity);
        }
    }
}
