package depromeet.lessonfour.server.recordquery.app.client;

import java.util.List;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.common.domain.vo.DailyExistence;

public interface ToiletRecordClient {

  List<DailyExistence> getDailyExistencesByActivityAtBetween(
      Long userId, ActivityAt start, ActivityAt end);

  int getToiletRecordCountByActivityAt(Long userId, ActivityAt activityAt);
}
