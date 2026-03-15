package org.example;

import org.example.domain.UnifiedEvent;
import org.example.pipeline.EventRefinery;
import org.example.schemas.RawEventV1;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationTest {
    private final EventRefinery refinery = new EventRefinery();

    @Test
    void testNoiseFilteringOutliers() {
        RawEventV1 v1 = new RawEventV1(
                "outlier", Instant.now(), "1.0", "TRF",
                new RawEventV1.Payload(300.0, 1, null, null, null, null) // Speed > 250
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

        Optional<UnifiedEvent> result = refinery.refine(v1);

        assertTrue(result.isEmpty());
    }
}
