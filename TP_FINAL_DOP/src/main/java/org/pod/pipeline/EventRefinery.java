package org.pod.pipeline;

import org.pod.Constants;
import org.pod.domain.ReportEvent;
import org.pod.domain.TrafficEvent;
import org.pod.domain.UnifiedEvent;
import org.pod.domain.WeatherEvent;
import org.pod.schemas.RawEvent;
import org.pod.schemas.RawEventV1;
import org.pod.schemas.RawEventV15;
import org.pod.schemas.RawEventV2;

import java.util.Optional;
import java.util.function.Supplier;

public class EventRefinery {

    public Optional<UnifiedEvent> refine(RawEvent event) {
        return switch (event) {
            case RawEventV1 v1 -> parseV1(v1);
            case RawEventV15 v15 -> parseV15(v15);
            case RawEventV2 v2 -> parseV2(v2);
        };
    }

    private <T extends UnifiedEvent> Optional<T> tryBuild(Supplier<T> builder) {
        try {
            return Optional.of(builder.get());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<UnifiedEvent> parseV1(RawEventV1 v1) {
        return Optional.ofNullable(v1.type())
            .map(String::toUpperCase)
            .flatMap(type -> Optional.ofNullable(v1.payload())
                .flatMap(payload -> switch (type) {
                    case "TRF" -> tryBuild(() -> new TrafficEvent(v1.id(), v1.timestamp(), 
                            payload.speed() != null ? payload.speed() : 0, 
                            payload.lane() != null ? payload.lane() : 0));
                    case "WTH" -> tryBuild(() -> new WeatherEvent(v1.id(), v1.timestamp(), 
                            payload.temperature() != null ? (payload.temperature() - 32) * 5 / 9.0 : 0, 
                            payload.humidity()));
                    case "REPORT" -> tryBuild(() -> new ReportEvent(v1.id(), v1.timestamp(), 
                            payload.category() != null ? payload.category() : Constants.SEVERITY_UNKNOWN,
                            Constants.SEVERITY_UNKNOWN,
                            payload.description() != null ? payload.description() : ""));
                    default -> Optional.empty();
                }));
    }

    private Optional<UnifiedEvent> parseV15(RawEventV15 v15) {
        return Optional.ofNullable(v15.kind())
            .map(String::toLowerCase)
            .flatMap(kind -> switch (kind) {
                case Constants.EVENT_TYPE_TRAFFIC -> tryBuild(() -> new TrafficEvent(
                        v15.id(), v15.timestamp(), v15.velocity(),
                        Optional.ofNullable(v15.attributes())
                                .map(RawEventV15.Attributes::laneId)
                                .map(l -> l.replace("L-", ""))
                                .map(Integer::parseInt)
                                .orElse(0)
                ));
                case Constants.EVENT_TYPE_WEATHER -> tryBuild(() -> new WeatherEvent(
                        v15.id(), v15.timestamp(), v15.temperatureC(), 
                        Optional.ofNullable(v15.attributes()).map(RawEventV15.Attributes::humidity).orElse(null)
                ));
                case Constants.EVENT_TYPE_REPORT -> tryBuild(() -> new ReportEvent(
                        v15.id(), v15.timestamp(), v15.category(),
                        Optional.ofNullable(v15.attributes()).map(RawEventV15.Attributes::severity).orElse(Constants.SEVERITY_UNKNOWN),
                        Optional.ofNullable(v15.attributes()).map(RawEventV15.Attributes::area).orElse("")
                ));
                default -> Optional.empty();
            });
    }

    private Optional<UnifiedEvent> parseV2(RawEventV2 v2) {
        return Optional.ofNullable(v2.eventType())
            .map(String::toUpperCase)
            .flatMap(type -> Optional.ofNullable(v2.data())
                .flatMap(data -> switch (type) {
                    case "TRAFFIC" -> tryBuild(() -> new TrafficEvent(v2.id(), v2.timestamp(), data.speedKmh(), data.lane()));
                    case "WEATHER" -> tryBuild(() -> new WeatherEvent(v2.id(), v2.timestamp(), data.temperature(), data.humidity()));
                    case "REPORT" -> tryBuild(() -> new ReportEvent(v2.id(), v2.timestamp(), data.category(), data.status(), ""));
                    default -> Optional.empty();
                }));
    }
}
