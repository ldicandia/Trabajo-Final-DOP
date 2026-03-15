package org.example.schemas;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RawEventDeserializer extends JsonDeserializer<RawEvent> {
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final List<Map.Entry<String, Class<? extends RawEvent>>> SCHEMA_SELECTORS = List.of(
            Map.entry("SCHEMA_VER", RawEventV1.class),
            Map.entry("version", RawEventV15.class),
            Map.entry("schemaVersion", RawEventV2.class)
    );

    @Override
    public RawEvent deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        Class<? extends RawEvent> schemaClass = SCHEMA_SELECTORS.stream()
                .filter(entry -> node.has(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown schema version for event: " + node));

        return mapper.treeToValue(node, schemaClass);
    }
}
