package org.pod.schemas;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RawEventDeserializer extends JsonDeserializer<RawEvent> {
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final DateTimeFormatter LOCAL_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final List<Map.Entry<String, Class<? extends RawEvent>>> SCHEMA_SELECTORS = List.of(
            Map.entry("SCHEMA_VER", RawEventV1.class),
            Map.entry("version", RawEventV15.class),
            Map.entry("schemaVersion", RawEventV2.class)
    );

    @Override
    public RawEvent deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        if (!(node instanceof ObjectNode objectNode)) {
            throw new IllegalArgumentException("Expected object event but got: " + node);
        }

        Class<? extends RawEvent> schemaClass = SCHEMA_SELECTORS.stream()
                .filter(entry -> findFieldNameIgnoreCase(objectNode, entry.getKey()).isPresent())
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown schema version for event: " + node));

        normalizeTimestampIfPresent(objectNode);
        return mapper.treeToValue(objectNode, schemaClass);
    }

    private static Optional<String> findFieldNameIgnoreCase(ObjectNode node, String expectedName) {
        var fieldNames = node.fieldNames();
        while (fieldNames.hasNext()) {
            String name = fieldNames.next();
            if (name.equalsIgnoreCase(expectedName)) {
                return Optional.of(name);
            }
        }
        return Optional.empty();
    }

    private static void normalizeTimestampIfPresent(ObjectNode node) {
        Optional<String> timestampField = findFieldNameIgnoreCase(node, "timestamp");
        if (timestampField.isEmpty()) {
            return;
        }

        JsonNode timestampNode = node.get(timestampField.get());
        if (timestampNode == null || timestampNode.isNull()) {
            return;
        }

        Instant parsedTimestamp = parseFlexibleTimestamp(timestampNode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid timestamp format: " + timestampNode.asText()));

        node.put("timestamp", parsedTimestamp.toString());
    }

    private static Optional<Instant> parseFlexibleTimestamp(JsonNode timestampNode) {
        if (timestampNode.isNumber()) {
            return Optional.of(Instant.ofEpochMilli(timestampNode.asLong()));
        }

        if (!timestampNode.isTextual()) {
            return Optional.empty();
        }

        String value = timestampNode.asText().trim();
        if (value.isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(Instant.parse(value));
        } catch (DateTimeParseException ignored) {
        }

        try {
            return Optional.of(OffsetDateTime.parse(value).toInstant());
        } catch (DateTimeParseException ignored) {
        }

        try {
            return Optional.of(LocalDateTime.parse(value, LOCAL_DATE_TIME).toInstant(ZoneOffset.UTC));
        } catch (DateTimeParseException ignored) {
        }

        return Optional.empty();
    }
}
