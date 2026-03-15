package org.example.domain;

import java.time.Instant;

public sealed interface UnifiedEvent permits TrafficEvent, WeatherEvent, ReportEvent {
    String id();
    Instant timestamp();
}
