package depromeet.lessonfour.server.toiletrecord.app.event;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.common.domain.event.CommonEvent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ToiletRecordEventPublisher {

  private final ApplicationEventPublisher delegate;
  private final Clock clock;

  public void publishCreated(String source, ToiletRecordEvent event) {
    publish("ToiletRecordCreated", source, event);
  }

  public void publishUpdated(String source, ToiletRecordEvent event) {
    publish("ToiletRecordUpdated", source, event);
  }

  private void publish(String eventType, String source, Object payload) {
    CommonEvent<Object> commonEvent =
        new CommonEvent<>(
            UUID.randomUUID(),
            eventType,
            source,
            LocalDateTime.now(clock),
            UUID.randomUUID().toString(),
            payload);

    delegate.publishEvent(commonEvent);
  }
}
