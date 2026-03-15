package org.pod.pipeline;

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
        return Optional.ofNullable(v1.TYPE())
            .map(String::toUpperCase)
            .flatMap(type -> Optional.ofNullable(v1.PAYLOAD())
                .flatMap(payload -> switch (type) {
                    case "TRF" -> tryBuild(() -> new TrafficEvent(v1.id(), v1.timestamp(), payload.SPD(), payload.LNE()));
                    case "WTH" -> tryBuild(() -> new WeatherEvent(v1.id(), v1.timestamp(), (payload.T() - 32) * 5 / 9.0, payload.H()));
                    case "REPORT" -> tryBuild(() -> new ReportEvent(v1.id(), v1.timestamp(), payload.CAT(), "UNKNOWN", payload.DESC()));
                    default -> Optional.empty();
                }));
    }

    private Optional<UnifiedEvent> parseV15(RawEventV15 v15) {
        return Optional.ofNullable(v15.kind())
            .map(String::toLowerCase)
            .flatMap(kind -> switch (kind) {
                case "traffic" -> tryBuild(() -> new TrafficEvent(
                        v15.id(), v15.timestamp(), v15.velocity(),
                        Optional.ofNullable(v15.attributes())
                                .map(RawEventV15.Attributes::lane_id)
                                .map(l -> l.replace("L-", ""))
                                .map(Integer::parseInt)
                                .orElse(0)
                ));
                case "weather" -> tryBuild(() -> new WeatherEvent(
                        v15.id(), v15.timestamp(), v15.temp_c(), 
                        Optional.ofNullable(v15.attributes()).map(RawEventV15.Attributes::HUMIDITY).orElse(null)
                ));
                case "report" -> tryBuild(() -> new ReportEvent(
                        v15.id(), v15.timestamp(), v15.category(),
                        Optional.ofNullable(v15.attributes()).map(RawEventV15.Attributes::severity).orElse("UNKNOWN"),
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
