package depromeet.lessonfour.server.recordquery.app.client;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.report.domain.vo.toilet.DailyToiletReport;

public interface ReportClient {

  int getScoreByActivityAt(Long userId, ActivityAt activityAt);

  DailyToiletReport getDailyToiletReportByActivityAt(Long userId, ActivityAt activityAt);
}
