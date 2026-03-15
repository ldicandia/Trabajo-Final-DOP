package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.analytics.AnalyticsEngine;
import org.example.analytics.AnalyticsReport;
import org.example.pipeline.EventRefinery;
import org.example.schemas.RawEvent;

import java.io.File;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
        
        com.fasterxml.jackson.databind.module.SimpleModule module = new com.fasterxml.jackson.databind.module.SimpleModule();
        module.addDeserializer(RawEvent.class, new org.example.schemas.RawEventDeserializer());
        mapper.registerModule(module);
        
        // Read raw events
        File inputFile = new File("example_input.json");
        if (!inputFile.exists()) {
            inputFile = new File("TP_FINAL_DOP/example_input.json");
        }
        
        if (!inputFile.exists()) {
            System.err.println("example_input.json not found in the current directory.");
            return;
        }

        List<RawEvent> events = mapper.readValue(inputFile, new TypeReference<>() {});
        
        // Initialize pipeline and engine
        EventRefinery refinery = new EventRefinery();
        AnalyticsEngine engine = new AnalyticsEngine(refinery);
        
        // Analyze
        AnalyticsReport report = engine.analyze(events);
        
        // Output metrics
        report.printReport();
    }
}
