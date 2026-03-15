package org.example.domain;

import java.time.Instant;

public record ReportEvent(
        String id,
        Instant timestamp,
        String category,
        String severity,
        String description
) implements UnifiedEvent {
}
