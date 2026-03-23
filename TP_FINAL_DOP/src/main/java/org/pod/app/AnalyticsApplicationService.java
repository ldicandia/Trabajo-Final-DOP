package org.pod.app;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.pod.analytics.AnalyticsEngine;
import org.pod.analytics.AnalyticsReport;
import org.pod.pipeline.EventRefinery;
import org.pod.schemas.RawEvent;
import org.pod.schemas.RawEventDeserializer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class AnalyticsApplicationService {
    private final ObjectMapper mapper;
    private final AnalyticsEngine analyticsEngine;

    public AnalyticsApplicationService() {
        this(new AnalyticsEngine(new EventRefinery()));
    }

    AnalyticsApplicationService(AnalyticsEngine analyticsEngine) {
        this.analyticsEngine = analyticsEngine;
        this.mapper = createMapper();
    }

    public AnalyticsReport analyze(Path inputPath) throws IOException {
        try (InputStream input = Files.newInputStream(inputPath)) {
            return analyze(input);
        }
    }

    public AnalyticsReport analyze(InputStream input) throws IOException {
        List<RawEvent> events = mapper.readValue(input, new TypeReference<>() {
        });
        return analyticsEngine.analyze(events);
    }

    private ObjectMapper createMapper() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        SimpleModule module = new SimpleModule();
        module.addDeserializer(RawEvent.class, new RawEventDeserializer());
        objectMapper.registerModule(module);
        return objectMapper;
    }
}

