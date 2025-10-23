package depromeet.lessonfour.server.recordquery.app.client;

import java.util.List;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.common.domain.vo.DailyExistence;

public interface ActivityRecordClient {

  List<DailyExistence> getDailyExistencesBetween(
      Long userId, ActivityAt startInclude, ActivityAt endInclude);

  int getActivityRecordCountByActivityAt(Long userId, ActivityAt activityAt);
}
