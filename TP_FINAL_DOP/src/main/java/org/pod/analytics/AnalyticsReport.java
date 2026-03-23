package org.pod.analytics;

import java.util.Map;

public record AnalyticsReport(
        long totalValidRecords,
        double averageTrafficSpeed,
        long totalCriticalEvents,
        Map<String, Long> schemaDistribution
) {
    public String toDisplayString() {
        StringBuilder builder = new StringBuilder();
        builder.append("=== CityTyci Data Refinery Report ===\n");
        builder.append("1. Total valid records processed: ")
                .append(totalValidRecords)
                .append('\n');
        builder.append(String.format("2. Average traffic speed: %.2f km/h%n", averageTrafficSpeed));
        builder.append("3. Total critical events detected: ")
                .append(totalCriticalEvents)
                .append('\n');
        builder.append("4. Schema Version Distribution:\n");
        schemaDistribution.forEach((version, count) ->
                builder.append("   - ").append(version).append(": ").append(count).append('\n'));
        builder.append("=====================================\n");
        return builder.toString();
    }

    public void printReport() {
        System.out.print(toDisplayString());
    }
}
