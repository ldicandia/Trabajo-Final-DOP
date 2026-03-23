package org.pod;

import org.junit.jupiter.api.Test;
import org.pod.analytics.AnalyticsReport;
import org.pod.app.AnalyticsApplicationService;

import java.io.InputStream;

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
}

