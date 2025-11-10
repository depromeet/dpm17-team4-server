package depromeet.lessonfour.server.report.app.service;

import java.time.LocalDateTime;

import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.report.app.dto.response.DailyReport;
import depromeet.lessonfour.server.report.domain.vo.activity.DailyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.toilet.DailyToiletReport;
import lombok.RequiredArgsConstructor;

@UseCase
@Transactional
@RequiredArgsConstructor
public class GetDailyReportUseCase {

  private final ActivityReportService activityReportService;
  private final ToiletReportService toiletReportService;

  public DailyReport getDailyReport(Long userId, LocalDateTime dateTime) {
    DailyActivityReport dailyActivityReport =
        activityReportService.generateDailyReport(userId, dateTime);
    DailyToiletReport dailyToiletReport = toiletReportService.generateDailyReport(userId, dateTime);

    return new DailyReport(dailyActivityReport, dailyToiletReport);
  }
}
