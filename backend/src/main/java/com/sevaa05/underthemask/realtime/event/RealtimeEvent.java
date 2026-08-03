package com.sevaa05.underthemask.realtime.event;

import java.time.Instant;

public record RealtimeEvent<T>(
        EventType type,
        T payload,
        Instant occurredAt
) {
}
