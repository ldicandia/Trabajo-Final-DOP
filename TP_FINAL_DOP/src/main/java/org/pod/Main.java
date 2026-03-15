package org.pod;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.pod.analytics.AnalyticsEngine;
import org.pod.analytics.AnalyticsReport;
import org.pod.pipeline.EventRefinery;
import org.pod.schemas.RawEvent;

import java.io.File;
import java.util.List;

import static java.lang.IO.println;

public class Main {
    static void main() throws Exception {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
        
        SimpleModule module = new SimpleModule();
        module.addDeserializer(RawEvent.class, new org.pod.schemas.RawEventDeserializer());
        mapper.registerModule(module);
        

        File inputFile = new File("example_input.json");
        if (!inputFile.exists()) {
            inputFile = new File("TP_FINAL_DOP/example_input.json");
        }
        if (!inputFile.exists()) {
            println("example_input.json not found in the current directory.");
            return;
        }

        List<RawEvent> events = mapper.readValue(inputFile, new TypeReference<>() {});

        EventRefinery refinery = new EventRefinery();
        AnalyticsEngine engine = new AnalyticsEngine(refinery);

        AnalyticsReport report = engine.analyze(events);

        report.printReport();
    }
}
