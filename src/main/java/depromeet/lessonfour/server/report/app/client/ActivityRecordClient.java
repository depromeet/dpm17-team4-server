package depromeet.lessonfour.server.report.app.client;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;

public interface ActivityRecordClient {

  Map<LocalDate, ActivityRecord> getActivityRecordsBetween(
      Long userId, LocalDateTime start, LocalDateTime end);
}
