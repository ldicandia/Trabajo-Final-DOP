package org.example;

import org.example.domain.WeatherEvent;
import org.example.domain.UnifiedEvent;
import org.example.pipeline.EventRefinery;
import org.example.schemas.RawEventV1;
import org.example.schemas.RawEventV15;
import org.example.schemas.RawEventV2;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationTest {
    private final EventRefinery refinery = new EventRefinery();

    @Test
    void testNoiseFilteringOutliers() {
        RawEventV1 v1 = new RawEventV1(
                "outlier", Instant.now(), "1.0", "TRF",
                new RawEventV1.Payload(600.0, 1, null, null, null, null) // Speed > 500
        );

        Optional<UnifiedEvent> result = refinery.refine(v1);

        assertTrue(result.isEmpty());
    }

    @Test
    void testNoiseFilteringNegativeOutliers() {
        RawEventV1 v1 = new RawEventV1(
                "negative", Instant.now(), "1.0", "TRF",
                new RawEventV1.Payload(-10.0, 1, null, null, null, null) // Speed < 0
        );
        assertTrue(refinery.refine(v1).isEmpty());
    }

    @Test
    void testMissingRequiredPayloadV1() {
        RawEventV1 v1 = new RawEventV1("nopayload", Instant.now(), "1.0", "TRF", null);
        assertTrue(refinery.refine(v1).isEmpty());
    }

    @Test
    void testMissingSpeedV15() {
        RawEventV15 v15 = new RawEventV15(
                "nospeed", Instant.now(), 1.5, "traffic", null, null, null, null
        );
        assertTrue(refinery.refine(v15).isEmpty());
    }

    @Test
    void testMissingCategoryV2() {
        RawEventV2 v2 = new RawEventV2(
                "2.0", "nocat", Instant.now(), "REPORT", 
                new RawEventV2.Data(null, null, null, null, null, "BROKEN")
        );
        assertTrue(refinery.refine(v2).isEmpty());
    }

    @Test
    void testHumidityOverOneHundredDiscarded() {
        RawEventV2 v2 = new RawEventV2(
                "2.0", "humid", Instant.now(), "WEATHER",
                new RawEventV2.Data(null, null, 20.0, 120.0, null, null)
        );
        assertTrue(refinery.refine(v2).isEmpty(), "Humidity > 100 should be discarded");
    }

    @Test
    void testNegativeHumidityDiscarded() {
        RawEventV2 v2 = new RawEventV2(
                "2.0", "neghumid", Instant.now(), "WEATHER",
                new RawEventV2.Data(null, null, 20.0, -5.0, null, null)
        );
        assertTrue(refinery.refine(v2).isEmpty(), "Negative humidity should be discarded");
    }

    @Test
    void testExtremeHighTemperatureDiscarded() {
        RawEventV15 v15 = new RawEventV15(
                "hightemp", Instant.now(), 1.5, "weather", null, 999.0, null, null
        );
        assertTrue(refinery.refine(v15).isEmpty(), "Temp > 60 should be discarded");
    }

    @Test
    void testExtremeLowTemperatureDiscarded() {
        RawEventV15 v15 = new RawEventV15(
                "lowtemp", Instant.now(), 1.5, "weather", null, -100.0, null, null
        );
        assertTrue(refinery.refine(v15).isEmpty(), "Temp < -90 should be discarded");
    }

    @Test
    void testV1FreezingPointConversion() {
        // 32°F should convert to exactly 0°C
        RawEventV1 v1 = new RawEventV1(
                "freeze", Instant.now(), "1.0", "WTH",
                new RawEventV1.Payload(null, null, 32.0, 50.0, null, null)
        );
        Optional<UnifiedEvent> result = refinery.refine(v1);
        assertTrue(result.isPresent());
        WeatherEvent w = (WeatherEvent) result.get();
        assertEquals(0.0, w.temperatureC(), 0.01, "32°F should be 0°C");
    }

    @Test
    void testV1NegativeFahrenheitConversion() {
        // -40°F should convert to -40°C (the crossover point)
        RawEventV1 v1 = new RawEventV1(
                "crossover", Instant.now(), "1.0", "WTH",
                new RawEventV1.Payload(null, null, -40.0, 50.0, null, null)
        );
        Optional<UnifiedEvent> result = refinery.refine(v1);
        assertTrue(result.isPresent());
        WeatherEvent w = (WeatherEvent) result.get();
        assertEquals(-40.0, w.temperatureC(), 0.01, "-40°F should be -40°C");
    }
}
