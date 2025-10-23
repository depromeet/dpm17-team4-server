package depromeet.lessonfour.server.recordquery.app.client;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;

public interface ReportClient {

  int getScoreByActivityAt(Long userId, ActivityAt activityAt);
}
