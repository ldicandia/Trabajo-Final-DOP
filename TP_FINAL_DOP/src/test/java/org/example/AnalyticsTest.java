package org.example;

import org.example.analytics.AnalyticsEngine;
import org.example.analytics.AnalyticsReport;
import org.example.pipeline.EventRefinery;
import org.example.schemas.RawEventV2;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnalyticsTest {

    @Test
    void testAnalyticsEngineMetrics() {
        AnalyticsEngine engine = new AnalyticsEngine(new EventRefinery());

        RawEventV2 e1 = new RawEventV2("2.0", "1", Instant.now(), "TRAFFIC",
                new RawEventV2.Data(100.0, 1, null, null, null, null));
        RawEventV2 e2 = new RawEventV2("2.0", "2", Instant.now(), "TRAFFIC",
                new RawEventV2.Data(50.0, 2, null, null, null, null));
        RawEventV2 e3 = new RawEventV2("2.0", "3", Instant.now(), "WEATHER",
                new RawEventV2.Data(null, null, 40.0, 50.0, null, null)); // CRITICAL weather (>35)

        AnalyticsReport report = engine.analyze(List.of(e1, e2, e3));

        assertEquals(3, report.totalValidRecords());
        assertEquals(75.0, report.averageTrafficSpeed());
        assertEquals(1, report.totalCriticalEvents());
        assertEquals(3, report.schemaDistribution().get("V2.0"));
    }
}
