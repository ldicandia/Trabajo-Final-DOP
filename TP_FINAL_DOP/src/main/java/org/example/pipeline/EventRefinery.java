package org.example.pipeline;

import org.example.domain.ReportEvent;
import org.example.domain.TrafficEvent;
import org.example.domain.UnifiedEvent;
import org.example.domain.WeatherEvent;
import org.example.schemas.RawEvent;
import org.example.schemas.RawEventV1;
import org.example.schemas.RawEventV15;
import org.example.schemas.RawEventV2;

import java.time.Instant;
import java.util.Optional;

public class EventRefinery {

    public Optional<UnifiedEvent> refine(RawEvent event) {
        return switch (event) {
            case RawEventV1 v1 -> parseV1(v1);
            case RawEventV15 v15 -> parseV15(v15);
            case RawEventV2 v2 -> parseV2(v2);
        };
    }

    private Optional<UnifiedEvent> parseV1(RawEventV1 v1) {
        String id = v1.id();
        Instant timestamp = v1.timestamp();
        var payload = v1.PAYLOAD();

        if (payload == null) return Optional.empty();

        return switch (v1.TYPE().toUpperCase()) {
            case "TRF" -> {
                if (payload.SPD() == null || payload.LNE() == null) yield Optional.empty();
                if (payload.SPD() < 0 || payload.SPD() > 250) yield Optional.empty();
                yield Optional.of(new TrafficEvent(id, timestamp, payload.SPD(), payload.LNE()));
            }
            case "WTH" -> {
                if (payload.T() == null) yield Optional.empty();
                // Convert Fahrenheit to Celsius
                double tempC = (payload.T() - 32) * 5 / 9.0;
                yield Optional.of(new WeatherEvent(id, timestamp, tempC, payload.H()));
            }
            case "REPORT" -> {
                if (payload.CAT() == null || payload.DESC() == null) yield Optional.empty();
                // V1 missing severity? We can assume UNKNOWN or infer it.
                yield Optional.of(new ReportEvent(id, timestamp, payload.CAT(), "UNKNOWN", payload.DESC()));
            }
            default -> Optional.empty();
        };
    }

    private Optional<UnifiedEvent> parseV15(RawEventV15 v15) {
        String id = v15.id();
        Instant timestamp = v15.timestamp();
        var attr = v15.attributes();

        return switch (v15.kind().toLowerCase()) {
            case "traffic" -> {
                if (v15.velocity() == null) yield Optional.empty();
                if (v15.velocity() < 0 || v15.velocity() > 250) yield Optional.empty();
                
                int lane = 0;
                if (attr != null && attr.lane_id() != null) {
                    try {
                        lane = Integer.parseInt(attr.lane_id().replace("L-", ""));
                    } catch (NumberFormatException ignored) {}
                }
                yield Optional.of(new TrafficEvent(id, timestamp, v15.velocity(), lane));
            }
            case "weather" -> {
                if (v15.temp_c() == null) yield Optional.empty();
                Double humidity = attr != null ? attr.HUMIDITY() : null;
                yield Optional.of(new WeatherEvent(id, timestamp, v15.temp_c(), humidity));
            }
            case "report" -> {
                if (v15.category() == null) yield Optional.empty();
                String severity = attr != null && attr.severity() != null ? attr.severity() : "UNKNOWN";
                String area = attr != null && attr.area() != null ? attr.area() : "";
                yield Optional.of(new ReportEvent(id, timestamp, v15.category(), severity, area));
            }
            default -> Optional.empty();
        };
    }

    private Optional<UnifiedEvent> parseV2(RawEventV2 v2) {
        String id = v2.id();
        Instant timestamp = v2.timestamp();
        var data = v2.data();
        if (data == null) return Optional.empty();

        return switch (v2.eventType().toUpperCase()) {
            case "TRAFFIC" -> {
                if (data.speedKmh() == null || data.lane() == null) yield Optional.empty();
                if (data.speedKmh() < 0 || data.speedKmh() > 250) yield Optional.empty();
                yield Optional.of(new TrafficEvent(id, timestamp, data.speedKmh(), data.lane()));
            }
            case "WEATHER" -> {
                if (data.temperature() == null) yield Optional.empty();
                yield Optional.of(new WeatherEvent(id, timestamp, data.temperature(), data.humidity()));
            }
            case "REPORT" -> {
                if (data.category() == null || data.status() == null) yield Optional.empty();
                yield Optional.of(new ReportEvent(id, timestamp, data.category(), data.status(), ""));
            }
            default -> Optional.empty();
        };
    }
}
