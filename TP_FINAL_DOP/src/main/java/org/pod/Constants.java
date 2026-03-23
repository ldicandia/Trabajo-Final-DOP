package org.pod;

/**
 * Global constants for event validation and categorization.
 */
public final class Constants {

    // Traffic validation
    public static final double MIN_VALID_SPEED_KMH = 0.0;
    public static final double MAX_VALID_SPEED_KMH = 500.0;
    
    // Weather validation
    public static final double MIN_VALID_TEMP_C = -90.0;
    public static final double MAX_VALID_TEMP_C = 60.0;
    public static final double MIN_VALID_HUMIDITY = 0.0;
    public static final double MAX_VALID_HUMIDITY = 100.0;
    
    // Critical event thresholds
    public static final double CRITICAL_TEMP_LOW_C = 0.0;
    public static final double CRITICAL_TEMP_HIGH_C = 35.0;
    
    // Report event severity constants
    public static final String SEVERITY_HIGH = "HIGH";
    public static final String SEVERITY_BROKEN = "BROKEN";
    public static final String SEVERITY_UNKNOWN = "UNKNOWN";
    
    // Report category keywords
    public static final String CATEGORY_POTHOLE = "pothole";
    public static final String CATEGORY_TRAFFIC_LIGHT = "traffic_light";
    public static final String CATEGORY_TRAFFIC_LIGHT_ALT = "traffic light";
    
    // Report area keywords (for severity assessment)
    public static final String AREA_KEYWORD_AVENUE = "avenue";
    public static final String AREA_KEYWORD_AVENIDA = "avenida";
    
    private Constants() {
        // Prevent instantiation
    }
}

