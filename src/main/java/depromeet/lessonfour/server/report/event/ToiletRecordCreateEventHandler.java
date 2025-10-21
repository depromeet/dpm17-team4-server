package depromeet.lessonfour.server.report.event;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.common.domain.event.CommonEvent;
import depromeet.lessonfour.server.report.app.service.StoolReportService;
import depromeet.lessonfour.server.toiletrecord.app.event.ToiletRecordEvent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ToiletRecordCreateEventHandler {

  private final StoolReportService stoolReportService;

  @Async
  @EventListener
  public void handle(CommonEvent<ToiletRecordEvent> event) {
    ToiletRecordEvent payload = event.data();
    stoolReportService.generateDailyReport(payload.userId(), payload.date().toDateTime());
  }
}
