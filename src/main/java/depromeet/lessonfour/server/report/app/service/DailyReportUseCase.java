package depromeet.lessonfour.server.report.app.service;

import java.time.LocalDateTime;

import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.report.domain.vo.DailyReport;
import depromeet.lessonfour.server.report.domain.vo.activity.DailyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.toilet.DailyToiletReport;
import lombok.RequiredArgsConstructor;

@UseCase
@Transactional
@RequiredArgsConstructor
public class DailyReportUseCase {

  private final ActivityReportService activityReportService;
  private final ToiletReportService toiletReportService;

  public DailyReport getDailyReport(Long userId, LocalDateTime dateTime) {
    ActivityAt activityAt = ActivityAt.of(dateTime.toLocalDate());

    // 생활 기록 리포트 생성
    DailyActivityReport dailyActivityReport =
        activityReportService.generateDailyReport(userId, activityAt);

    // 배변 기록 리포트 생성
    DailyToiletReport dailyToiletReport =
        toiletReportService.generateDailyReport(userId, activityAt);

    return new DailyReport(dailyActivityReport, dailyToiletReport);
  }
}
