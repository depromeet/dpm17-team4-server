package depromeet.lessonfour.server.report.domain.repository;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.report.domain.entity.ToiletScore;

public interface ToiletScoreRepository {

  int getScoreByActivityAt(Long userId, ActivityAt activityAt);

  void save(ToiletScore toiletScore);
}
