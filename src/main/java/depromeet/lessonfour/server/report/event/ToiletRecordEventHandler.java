package depromeet.lessonfour.server.report.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.common.domain.event.CommonEvent;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.report.app.service.StoolReportService;
import depromeet.lessonfour.server.toiletrecord.app.event.ToiletRecordEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ToiletRecordEventHandler {

  private final StoolReportService stoolReportService;

  @EventListener
  public void handle(CommonEvent<?> event) {
    if (!(event.payload() instanceof ToiletRecordEvent payload)) {
      return;
    }

    Long userId = payload.userId();
    ActivityAt date = payload.date();

    log.info("Handling ToiletRecord event - traceId: {}", event.traceId());
    stoolReportService.generateDailyReport(userId, date.toDateTime());
    log.info("ToiletScore saved - traceId: {}", event.traceId());
  }
}
