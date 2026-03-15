package org.pod.schemas;

public sealed interface RawEvent permits RawEventV1, RawEventV15, RawEventV2 {
}
