package org.example;

import org.example.domain.UnifiedEvent;
import org.example.pipeline.EventRefinery;
import org.example.schemas.RawEventV1;
import org.example.schemas.RawEventV15;
import org.example.schemas.RawEventV2;
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
}
