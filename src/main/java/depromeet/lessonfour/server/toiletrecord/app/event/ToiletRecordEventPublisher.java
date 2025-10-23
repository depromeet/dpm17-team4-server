package depromeet.lessonfour.server.toiletrecord.app.event;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.common.domain.event.CommonEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ToiletRecordEventPublisher {

  private final ApplicationEventPublisher delegate;
  private final Clock clock;

  public void publishCreated(String source, ToiletRecordEvent event) {
    publishWithLog("ToiletRecordCreated", source, event);
  }

  public void publishUpdated(String source, ToiletRecordEvent event) {
    publishWithLog("ToiletRecordUpdated", source, event);
  }

  public void publishDeleted(String source, ToiletRecordEvent event) {
    publishWithLog("ToiletRecordDeleted", source, event);
  }

  private void publishWithLog(String eventType, String source, ToiletRecordEvent payload) {
    UUID eventId = UUID.randomUUID();
    String traceId = UUID.randomUUID().toString();
    LocalDateTime occurredAt = LocalDateTime.now(clock);

    CommonEvent<ToiletRecordEvent> commonEvent =
        new CommonEvent<>(eventId, eventType, source, occurredAt, traceId, payload);

    log.info(
        "[{}] Publishing event - type={}, source={}, userId={}, date={}, occurredAt={}",
        traceId,
        eventType,
        source,
        payload.userId(),
        payload.date(),
        occurredAt);

    delegate.publishEvent(commonEvent);
  }
}
