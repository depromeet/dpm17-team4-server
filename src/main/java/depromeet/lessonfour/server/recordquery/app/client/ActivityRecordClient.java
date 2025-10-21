package depromeet.lessonfour.server.recordquery.app.client;

import java.util.List;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.common.domain.vo.DailyExistence;

public interface ActivityRecordClient {

  List<DailyExistence> existsByActivityAtBetween(Long userId, ActivityAt start, ActivityAt end);

  int getActivityRecordCountByActivityAt(Long userId, ActivityAt activityAt);
}
