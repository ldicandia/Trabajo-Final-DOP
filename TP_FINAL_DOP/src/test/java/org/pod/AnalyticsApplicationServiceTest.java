package org.pod;

import org.junit.jupiter.api.Test;
import org.pod.analytics.AnalyticsReport;
import org.pod.app.AnalyticsApplicationService;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnalyticsApplicationServiceTest {

    @Test
    void testAnalyzeFromStream() throws Exception {
        AnalyticsApplicationService service = new AnalyticsApplicationService();

        InputStream input = getClass().getClassLoader().getResourceAsStream("test_input.json");
        assertNotNull(input, "test_input.json must be available");

        AnalyticsReport report;
        try (input) {
            report = service.analyze(input);
        }

        assertTrue(report.totalValidRecords() > 0);
        assertTrue(report.schemaDistribution().containsKey("V1.0"));
        assertTrue(report.schemaDistribution().containsKey("V1.5"));
        assertTrue(report.schemaDistribution().containsKey("V2.0"));
    }

    @Test
    void testAnalyzeSkipsInvalidRecordsAndProcessesValidOnes() throws Exception {
        AnalyticsApplicationService service = new AnalyticsApplicationService();

        String json = """
                [
                  {
                    "id": "ok-1",
                    "timestamp": "2026-03-15T00:00:00Z",
                    "version": 1.5,
                    "kind": "TRAFFIC",
                    "velocity": 80.0,
                    "attributes": {
                      "lane_id": "1"
                    }
                  },
                  {
                    "id": "bad-1",
                    "timestamp": "15/03/2026 00:00:00",
                    "version": 1.5,
                    "kind": "TRAFFIC",
                    "velocity": 70.0,
                    "attributes": {
                      "lane_id": "2"
                    }
                  }
                ]
                """;

        try (InputStream input = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))) {
            AnalyticsReport report = service.analyze(input);
            assertTrue(report.totalValidRecords() >= 1);
            assertTrue(report.schemaDistribution().containsKey("V1.5"));
        }
    }
}

