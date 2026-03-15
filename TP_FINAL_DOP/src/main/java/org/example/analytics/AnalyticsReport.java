package org.example.analytics;

import java.util.Map;

public record AnalyticsReport(
        long totalValidRecords,
        double averageTrafficSpeed,
        long totalCriticalEvents,
        Map<String, Long> schemaDistribution
) {
    public void printReport() {
        System.out.println("=== CityTyci Data Refinery Report ===");
        System.out.println("1. Total valid records processed: " + totalValidRecords);
        System.out.printf("2. Average traffic speed: %.2f km/h\n", averageTrafficSpeed);
        System.out.println("3. Total critical events detected: " + totalCriticalEvents);
        System.out.println("4. Schema Version Distribution:");
        schemaDistribution.forEach((version, count) ->
                System.out.println("   - " + version + ": " + count));
        System.out.println("=====================================");
    }
}
