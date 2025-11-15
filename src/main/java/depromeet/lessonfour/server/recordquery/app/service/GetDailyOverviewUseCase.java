package depromeet.lessonfour.server.recordquery.app.service;

import java.time.LocalDate;

import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.recordquery.app.client.ActivityRecordClient;
import depromeet.lessonfour.server.recordquery.app.client.ReportClient;
import depromeet.lessonfour.server.recordquery.app.dto.DailyOverviewDto;
import depromeet.lessonfour.server.report.domain.vo.toilet.DailyToiletReport;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class GetDailyOverviewUseCase {

  private final ReportClient reportClient;
  private final ActivityRecordClient activityRecordClient;

  public DailyOverviewDto getDailyOverview(Long userId, LocalDate date) {
    ActivityAt activityAt = ActivityAt.of(date);

    // 배변 리포트 조회
    DailyToiletReport dailyReport =
        reportClient.getDailyToiletReportByActivityAt(userId, activityAt);

    // 생활 기록 존재 여부 조회
    boolean activityRecordExists = activityRecordClient.existsByActivityAt(userId, activityAt);

    return new DailyOverviewDto(dailyReport.getLevel(), dailyReport.size(), activityRecordExists);
  }
}
