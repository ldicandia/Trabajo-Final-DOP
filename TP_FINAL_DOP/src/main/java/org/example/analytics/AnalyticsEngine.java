package org.example.analytics;

import org.example.domain.ReportEvent;
import org.example.domain.TrafficEvent;
import org.example.domain.UnifiedEvent;
import org.example.domain.WeatherEvent;
import org.example.pipeline.EventRefinery;
import org.example.schemas.RawEvent;
import org.example.schemas.RawEventV1;
import org.example.schemas.RawEventV15;
import org.example.schemas.RawEventV2;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AnalyticsEngine {
    private final EventRefinery refinery;

    public AnalyticsEngine(EventRefinery refinery) {
        this.refinery = refinery;
    }

    public AnalyticsReport analyze(List<RawEvent> rawEvents) {
        // 4. Schema distribution
        Map<String, Long> schemaDistribution = rawEvents.stream()
                .collect(Collectors.groupingBy(e -> switch (e) {
                    case RawEventV1 v1 -> "V1.0";
                    case RawEventV15 v15 -> "V1.5";
                    case RawEventV2 v2 -> "V2.0";
                }, Collectors.counting()));

        // Refine events
        List<UnifiedEvent> validEvents = rawEvents.stream()
                .map(refinery::refine)
                .flatMap(Optional::stream)
                .toList();

        // 1. Total valid
        long totalValid = validEvents.size();

        // 2. Average speed (Traffic only)
        double averageSpeed = validEvents.stream()
                .filter(e -> e instanceof TrafficEvent)
                .map(e -> (TrafficEvent) e)
                .mapToDouble(TrafficEvent::speedKmh)
                .average()
                .orElse(0.0);

        // 3. Critical events
        long criticalEvents = validEvents.stream()
                .filter(this::isCritical)
                .count();

        return new AnalyticsReport(totalValid, averageSpeed, criticalEvents, schemaDistribution);
    }

    private boolean isCritical(UnifiedEvent event) {
        return switch (event) {
            case WeatherEvent w -> w.temperatureC() < 0 || w.temperatureC() > 35;
            case ReportEvent r -> {
                String cat = r.category() != null ? r.category().toLowerCase() : "";
                String desc = r.description() != null ? r.description().toLowerCase() : "";
                String sev = r.severity() != null ? r.severity().toUpperCase() : "";

                boolean isPothole = cat.contains("pothole");
                boolean severePothole = isPothole && (sev.equals("HIGH") || desc.contains("avenue") || desc.contains("avenida"));

                boolean isBrokenLight = (cat.contains("traffic_light") || cat.contains("traffic light")) 
                        && sev.contains("BROKEN");

                yield severePothole || isBrokenLight;
            }
            case TrafficEvent t -> false; // No critical traffic events explicitly defined
        };
    }
}
