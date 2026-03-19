package org.pod.domain;

import org.pod.Constants;

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
        if (temperatureC < Constants.MIN_VALID_TEMP_C || temperatureC > Constants.MAX_VALID_TEMP_C) {
            throw new IllegalArgumentException("Temperature out of range: " + temperatureC);
        }
        if (humidity != null && (humidity < Constants.MIN_VALID_HUMIDITY || humidity > Constants.MAX_VALID_HUMIDITY)) {
            throw new IllegalArgumentException("Humidity out of range: " + humidity);
        }
    }
}
