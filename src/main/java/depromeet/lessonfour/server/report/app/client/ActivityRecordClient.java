package depromeet.lessonfour.server.report.app.client;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;

public interface ActivityRecordClient {

  Map<LocalDate, ActivityRecord> getActivityRecordsBetween(
      Long userId, LocalDateTime start, LocalDateTime end);

  List<ActivityRecord> getActivityRecordsBetween(Long userId, ActivityAt startAt, ActivityAt endAt);
}
