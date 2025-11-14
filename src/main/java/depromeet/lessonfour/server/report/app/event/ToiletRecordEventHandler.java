package depromeet.lessonfour.server.report.app.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.common.domain.event.CommonEvent;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.report.app.service.ToiletScoreUpsertUseCase;
import depromeet.lessonfour.server.toiletrecord.app.event.ToiletRecordEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ToiletRecordEventHandler {

  private final ToiletScoreUpsertUseCase toiletScoreUpsertUseCase;

  @EventListener
  public void handle(CommonEvent<?> event) {
    if (!(event.payload() instanceof ToiletRecordEvent payload)) {
      return;
    }

    Long userId = payload.userId();
    ActivityAt date = payload.date();

    log.info("Handling ToiletRecord event - traceId: {}", event.traceId());
    toiletScoreUpsertUseCase.upsertToiletScore(userId, date);
    log.info("ToiletScore saved - traceId: {}", event.traceId());
  }
}
