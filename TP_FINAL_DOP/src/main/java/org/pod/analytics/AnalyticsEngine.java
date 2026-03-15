package org.pod.analytics;

import org.pod.domain.ReportEvent;
import org.pod.domain.TrafficEvent;
import org.pod.domain.UnifiedEvent;
import org.pod.domain.WeatherEvent;
import org.pod.pipeline.EventRefinery;
import org.pod.schemas.RawEvent;
import org.pod.schemas.RawEventV1;
import org.pod.schemas.RawEventV15;
import org.pod.schemas.RawEventV2;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class AnalyticsEngine {
    private final EventRefinery refinery;

    public AnalyticsEngine(EventRefinery refinery) {
        this.refinery = refinery;
    }

    public AnalyticsReport analyze(List<RawEvent> rawEvents) {
        Map<String, Long> schemaDistribution = rawEvents.stream()
                .collect(Collectors.groupingBy(e -> switch (e) {
                    case RawEventV1 _ -> "V1.0";
                    case RawEventV15 _ -> "V1.5";
                    case RawEventV2 _ -> "V2.0";
                }, Collectors.counting()));

        List<UnifiedEvent> validEvents = rawEvents.stream()
                .map(refinery::refine)
                .flatMap(Optional::stream)
                .toList();

        long totalValid = validEvents.size();

        double averageSpeed = validEvents.stream()
                .flatMapToDouble(event -> switch (event) {
                    case TrafficEvent t -> DoubleStream.of(t.speedKmh());
                    case WeatherEvent ignored -> DoubleStream.empty();
                    case ReportEvent ignored -> DoubleStream.empty();
                })
                .average()
                .orElse(0.0);

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
            case TrafficEvent ignored -> false;
        };
    }
}
