package depromeet.lessonfour.server.report.app.service;

import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.report.domain.vo.toilet.DailyToiletReport;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class ToiletScoreUpsertUseCase {

  private final ToiletReportService toiletReportService;
  private final ToiletScoreService toiletScoreService;

  public void upsertToiletScore(Long userId, ActivityAt activityAt) {

    // 일간 배변 리포트 생성
    DailyToiletReport dailyToiletReport =
        toiletReportService.generateDailyReport(userId, activityAt);

    // 배변 점수 업데이트
    toiletScoreService.upsertScore(userId, (int) dailyToiletReport.getToiletScore(), activityAt);
  }
}
