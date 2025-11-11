package depromeet.lessonfour.server.report.app.client;

import java.util.List;

import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;

public interface ActivityRecordClient {

  List<ActivityRecord> getActivityRecordsBetween(Long userId, ActivityAt startAt, ActivityAt endAt);
}
