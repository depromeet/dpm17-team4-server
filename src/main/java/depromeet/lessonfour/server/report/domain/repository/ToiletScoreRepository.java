package depromeet.lessonfour.server.report.domain.repository;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;

public interface ToiletScoreRepository {

  int getScoreByActivityAt(Long userId, ActivityAt activityAt);
}
