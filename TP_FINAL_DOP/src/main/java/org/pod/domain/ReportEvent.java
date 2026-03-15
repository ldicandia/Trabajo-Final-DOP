package org.pod.domain;

import java.time.Instant;
import java.util.Objects;

public record ReportEvent(
        String id,
        Instant timestamp,
        String category,
        String severity,
        String description
) implements UnifiedEvent {
    public ReportEvent {
        Objects.requireNonNull(id);
        Objects.requireNonNull(timestamp);
        Objects.requireNonNull(category);
        Objects.requireNonNull(severity);
        Objects.requireNonNull(description);
    }
}
