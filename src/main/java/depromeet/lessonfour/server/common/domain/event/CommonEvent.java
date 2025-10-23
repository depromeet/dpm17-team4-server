package depromeet.lessonfour.server.common.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommonEvent<T>(
    UUID eventId,
    String eventType,
    String source,
    LocalDateTime occurredAt,
    String traceId,
    T payload) {

  public CommonEvent {
    if (eventId == null) {
      throw new IllegalArgumentException("eventId must not be null");
    }
    if (eventType == null || eventType.isBlank()) {
      throw new IllegalArgumentException("eventType must not be null or blank");
    }
    if (occurredAt == null) {
      throw new IllegalArgumentException("occurredAt must not be null");
    }
    if (payload == null) {
      throw new IllegalArgumentException("payload must not be null");
    }
  }
}
