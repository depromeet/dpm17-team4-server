package depromeet.lessonfour.server.recordquery.infra;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.recordquery.app.client.ReportClient;
import depromeet.lessonfour.server.report.app.service.ToiletReportService;
import depromeet.lessonfour.server.report.app.service.ToiletScoreService;
import depromeet.lessonfour.server.report.domain.vo.toilet.DailyToiletReport;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReportClientImpl implements ReportClient {

  private final ToiletReportService toiletReportService;
  private final ToiletScoreService toiletScoreService;

  @Override
  public int getScoreByActivityAt(Long userId, ActivityAt activityAt) {
    return toiletScoreService.getScoreByActivityAt(userId, activityAt);
  }

  @Override
  public DailyToiletReport getDailyToiletReportByActivityAt(Long userId, ActivityAt activityAt) {
    return toiletReportService.generateDailyReport(userId, activityAt);
  }
}
