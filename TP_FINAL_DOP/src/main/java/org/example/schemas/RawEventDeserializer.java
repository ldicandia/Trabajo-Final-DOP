package org.example.schemas;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;

public class RawEventDeserializer extends JsonDeserializer<RawEvent> {
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Override
    public RawEvent deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        if (node.has("SCHEMA_VER")) {
            return mapper.treeToValue(node, RawEventV1.class);
        } else if (node.has("version")) {
            return mapper.treeToValue(node, RawEventV15.class);
        } else if (node.has("schemaVersion")) {
            return mapper.treeToValue(node, RawEventV2.class);
        }

        throw new IllegalArgumentException("Unknown schema version for event: " + node);
    }
}
