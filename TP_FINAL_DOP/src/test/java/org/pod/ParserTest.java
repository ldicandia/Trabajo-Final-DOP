package org.pod;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.pod.domain.TrafficEvent;
import org.pod.domain.UnifiedEvent;
import org.pod.domain.WeatherEvent;
import org.pod.pipeline.EventRefinery;
import org.pod.schemas.RawEvent;
import org.pod.schemas.RawEventDeserializer;
import org.pod.schemas.RawEventV1;
import org.pod.schemas.RawEventV15;
import org.pod.schemas.RawEventV2;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {
    private final EventRefinery refinery = new EventRefinery();

    private ObjectMapper rawEventMapper() {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        SimpleModule module = new SimpleModule();
        module.addDeserializer(RawEvent.class, new RawEventDeserializer());
        mapper.registerModule(module);
        return mapper;
    }

    @Test
    void testV1WeatherFahrenheitToCelsius() {
        RawEventV1 v1 = new RawEventV1(
                "123", Instant.now(), "1.0", "WTH",
                new RawEventV1.Payload(null, null, 95.0, 40.0, null, null)
        );

        Optional<UnifiedEvent> result = refinery.refine(v1);

        assertTrue(result.isPresent());
        assertInstanceOf(WeatherEvent.class, result.get());
        WeatherEvent weather = (WeatherEvent) result.get();
        assertEquals(35.0, weather.temperatureC(), 0.01);
    }

    @Test
    void testV1TrafficParsing() {
        RawEventV1 v1 = new RawEventV1(
                "456", Instant.now(), "1.0", "TRF",
                new RawEventV1.Payload(65.5, 1, null, null, null, null)
        );

        Optional<UnifiedEvent> result = refinery.refine(v1);

        assertTrue(result.isPresent());
        assertInstanceOf(TrafficEvent.class, result.get());
        TrafficEvent traffic = (TrafficEvent) result.get();
        assertEquals(65.5, traffic.speedKmh());
        assertEquals(1, traffic.lane());
    }

    @Test
    void testDeserializerRoutesV15ByVersionField() throws Exception {
        String json = """
                {
                  "id": "v15-1",
                  "timestamp": "2026-03-15T00:00:00Z",
                  "version": 1.5,
                  "kind": "TRAFFIC",
                  "velocity": 70.0,
                  "temp_c": null,
                  "category": null,
                  "attributes": {
                    "lane_id": "2",
                    "HUMIDITY": null,
                    "severity": null,
                    "area": null
                  }
                }
                """;

        RawEvent event = rawEventMapper().readValue(json, RawEvent.class);
        assertInstanceOf(RawEventV15.class, event);
    }

    @Test
    void testDeserializerPrioritizesV1WhenMultipleSchemaFieldsExist() throws Exception {
        String json = """
                {
                  "id": "mixed-1",
                  "timestamp": "2026-03-15T00:00:00Z",
                  "SCHEMA_VER": "1.0",
                  "version": 1.5,
                  "schemaVersion": "2.0",
                  "TYPE": "TRF",
                  "PAYLOAD": {
                    "SPD": 60.0,
                    "LNE": 1,
                    "T": null,
                    "H": null,
                    "CAT": null,
                    "DESC": null
                  }
                }
                """;

        RawEvent event = rawEventMapper().readValue(json, RawEvent.class);
        assertInstanceOf(RawEventV1.class, event);
    }

    @Test
    void testDeserializerThrowsForUnknownSchema() {
        String json = """
                {
                  "id": "unknown-1",
                  "timestamp": "2026-03-15T00:00:00Z",
                  "kind": "TRAFFIC"
                }
                """;

        assertThrows(IllegalArgumentException.class, () -> rawEventMapper().readValue(json, RawEvent.class));
    }

    @Test
    void testDeserializerDetectsSchemaKeyIgnoringCase() throws Exception {
        String json = """
                {
                  "id": "v2-case",
                  "TimeStamp": "2026-03-15T00:00:00Z",
                  "SCHEMAVERSION": "2.0",
                  "eventType": "TRAFFIC",
                  "data": {
                    "speedKmh": 81.0,
                    "lane": 1,
                    "temperature": null,
                    "humidity": null,
                    "category": null,
                    "status": null
                  }
                }
                """;

        RawEvent event = rawEventMapper().readValue(json, RawEvent.class);
        assertInstanceOf(RawEventV2.class, event);
    }

    @Test
    void testDeserializerAcceptsTimestampWithSpaceSeparator() throws Exception {
        String json = """
                {
                  "id": "v15-time-1",
                  "timestamp": "2026-03-15 00:00:00",
                  "version": 1.5,
                  "kind": "TRAFFIC",
                  "velocity": 60.0,
                  "temp_c": null,
                  "category": null,
                  "attributes": {
                    "lane_id": "2",
                    "HUMIDITY": null,
                    "severity": null,
                    "area": null
                  }
                }
                """;

        RawEvent event = rawEventMapper().readValue(json, RawEvent.class);
        assertInstanceOf(RawEventV15.class, event);
    }
}
