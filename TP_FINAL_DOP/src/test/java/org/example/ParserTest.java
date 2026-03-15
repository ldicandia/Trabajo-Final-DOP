package org.example;

import org.example.domain.TrafficEvent;
import org.example.domain.UnifiedEvent;
import org.example.domain.WeatherEvent;
import org.example.pipeline.EventRefinery;
import org.example.schemas.RawEventV1;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {
    private final EventRefinery refinery = new EventRefinery();

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
}
