package depromeet.lessonfour.server.common.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommonEvent<T>(
    UUID eventId,
    String eventType,
    String source,
    LocalDateTime occurredAt,
    String traceId,
    T payload) {}
