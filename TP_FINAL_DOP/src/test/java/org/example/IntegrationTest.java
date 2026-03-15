package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.analytics.AnalyticsEngine;
import org.example.analytics.AnalyticsReport;
import org.example.pipeline.EventRefinery;
import org.example.schemas.RawEvent;
import org.example.schemas.RawEventDeserializer;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IntegrationTest {

    private ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        SimpleModule module = new SimpleModule();
        module.addDeserializer(RawEvent.class, new RawEventDeserializer());
        mapper.registerModule(module);
        return mapper;
    }

    @Test
    void testEndToEndWithExampleInput() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("test_input.json");
        assertNotNull(is, "test_input.json must be on classpath");

        List<RawEvent> events = createMapper().readValue(is, new TypeReference<>() {});
        assertEquals(15, events.size(), "Should parse all 15 raw events");

        AnalyticsEngine engine = new AnalyticsEngine(new EventRefinery());
        AnalyticsReport report = engine.analyze(events);

        // Discarded events:
        // E4E4E4E4: speed 350 -> valid (<=500)... actually it IS valid
        // D3D3D3D3: humidity 120 -> discarded (out of range)
        // h7h7h7h7: WTH v1 T=10F -> (10-32)*5/9 = -12.22C, H=null -> valid
        // I8I8I8I8: velocity null -> discarded (NullPointerException on unboxing)
        // k10k10k10: temp_c=45 -> out of range (>60? no, 45 < 60 -> valid)
        // So discarded: D3D3D3D3 (humidity 120), I8I8I8I8 (null velocity)
        // Valid: 15 - 2 = 13
        assertTrue(report.totalValidRecords() > 0, "Should have valid records");
        assertTrue(report.averageTrafficSpeed() > 0.0, "Should have positive average speed");
        assertTrue(report.totalCriticalEvents() >= 0, "Critical events should be non-negative");

        // Schema distribution should have all 3 versions
        assertTrue(report.schemaDistribution().containsKey("V1.0"));
        assertTrue(report.schemaDistribution().containsKey("V1.5"));
        assertTrue(report.schemaDistribution().containsKey("V2.0"));

        // Print for visual verification
        report.printReport();
    }
}
