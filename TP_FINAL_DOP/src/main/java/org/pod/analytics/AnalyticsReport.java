package org.pod.analytics;

import java.util.Map;

import static java.lang.IO.println;

public record AnalyticsReport(
        long totalValidRecords,
        double averageTrafficSpeed,
        long totalCriticalEvents,
        Map<String, Long> schemaDistribution
) {
    public void printReport() {
        println("=== CityTyci Data Refinery Report ===");
        println("1. Total valid records processed: " + totalValidRecords);
        System.out.printf("2. Average traffic speed: %.2f km/h\n", averageTrafficSpeed);
        println("3. Total critical events detected: " + totalCriticalEvents);
        println("4. Schema Version Distribution:");
        schemaDistribution.forEach((version, count) ->
                println("   - " + version + ": " + count));
        println("=====================================");
    }
}
