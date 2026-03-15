# Trabajo Final - DOP (CityTyci Data Refinery)

Java + Maven project that ingests mixed-version city events, normalizes them into a single domain model, filters invalid/outlier records, and generates an analytics report.

## What this project does

- Reads a JSON array of raw events where each record may follow schema `1.0`, `1.5`, or `2.0`.
- Detects schema version per record dynamically (`RawEventDeserializer`).
- Converts each raw record into a normalized `UnifiedEvent` (`EventRefinery`).
- Discards malformed/noisy events through domain validations.
- Computes analytics (`AnalyticsEngine`) and prints a final report (`AnalyticsReport`).

## Tech stack

- Java (configured in `TP_FINAL_DOP/pom.xml` with source/target `25`)
- Maven
- Jackson (`jackson-databind`, `jackson-datatype-jsr310`)
- JUnit 5

## Project layout

```text
TP_FINAL_DOP/
  src/main/java/org/pod/
	Main.java                        # App entry flow
	schemas/                         # Raw schema models + polymorphic deserializer
	pipeline/EventRefinery.java      # Schema-specific mapping -> UnifiedEvent
	domain/                          # Unified validated event records
	analytics/                       # Metrics computation + report output
  src/test/java/org/pod/             # Parser, validation, analytics, integration tests
  example_input.json                 # Sample mixed-schema input
```

## End-to-end flow

1. `Main` configures Jackson (including Java time support and `RawEventDeserializer`).
2. Input file is resolved from either:
   - `example_input.json` (current directory), or
   - `TP_FINAL_DOP/example_input.json` (workspace root execution).
3. JSON list is deserialized into `List<RawEvent>`.
4. `AnalyticsEngine` calls `EventRefinery` to transform each raw event into `Optional<UnifiedEvent>`.
5. Invalid/outlier records become empty optionals and are excluded.
6. Analytics are computed from valid normalized events and printed.

## Supported schemas and routing

`RawEventDeserializer` routes by first matching key in this priority order:

1. `SCHEMA_VER` -> `RawEventV1`
2. `version` -> `RawEventV15`
3. `schemaVersion` -> `RawEventV2`

If none are present, deserialization throws `IllegalArgumentException`.

## Normalization rules (EventRefinery)

- **V1 (`SCHEMA_VER`, `TYPE`, `PAYLOAD`)**
  - `TRF` -> `TrafficEvent(SPD, LNE)`
  - `WTH` -> `WeatherEvent((T - 32) * 5 / 9, H)` (Fahrenheit to Celsius)
  - `REPORT` -> `ReportEvent(CAT, "UNKNOWN", DESC)`
- **V1.5 (`version`, `kind`)**
  - `traffic` -> `TrafficEvent(velocity, lane_id)` where `lane_id` strips `L-`
  - `weather` -> `WeatherEvent(temp_c, HUMIDITY)`
  - `report` -> `ReportEvent(category, severity, area)`
- **V2 (`schemaVersion`, `eventType`, `data`)**
  - `TRAFFIC` -> `TrafficEvent(speedKmh, lane)`
  - `WEATHER` -> `WeatherEvent(temperature, humidity)`
  - `REPORT` -> `ReportEvent(category, status, "")`

`EventRefinery` uses safe construction (`tryBuild`) so invalid data is dropped instead of breaking the full pipeline.

## Validation and noise filtering

The domain records enforce constraints via constructors:

- `TrafficEvent`: speed must be `0..500` km/h
- `WeatherEvent`: temperature must be `-90..60` C, humidity `0..100` when present
- `ReportEvent`: non-null required fields

Any violation throws, then `EventRefinery` catches and discards that event.

## Analytics produced

`AnalyticsEngine` returns `AnalyticsReport` with:

1. `totalValidRecords`
2. `averageTrafficSpeed` (from traffic events only)
3. `totalCriticalEvents`
4. `schemaDistribution` (`V1.0`, `V1.5`, `V2.0`) over raw input

Critical event rules:

- `WeatherEvent`: temperature `< 0` or `> 35`
- `ReportEvent`:
  - severe pothole (`category` contains `pothole` and severity `HIGH`, or description mentions `avenue`/`avenida`)
  - broken traffic light (`category` contains `traffic_light`/`traffic light` and severity contains `BROKEN`)

## Tests

Test coverage is split by concern:

- `ParserTest`: schema routing + parsing behaviors
- `ValidationTest`: outlier/missing field filtering and conversion checks
- `AnalyticsTest`: metric and critical-event logic
- `IntegrationTest`: end-to-end execution over `test_input.json`

Run tests (from repository root):

```powershell
Set-Location "TP_FINAL_DOP"
mvn test
```

## Run the application

If Maven and a compatible JDK are installed, run from repository root:

```powershell
Set-Location "TP_FINAL_DOP"
mvn -DskipTests package
```

Then run `org.pod.Main` from your IDE, or use your preferred Java launcher setup for the compiled classes.

## Notes

- The repository also includes `Trabajo Final.pdf` with assignment/context.
- Sample input file with mixed schemas is `TP_FINAL_DOP/example_input.json`.
