package depromeet.lessonfour.server.report.app.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;

public interface ActivityRecordService {

  Map<LocalDate, ActivityRecord> getActivityRecordsBetween(
      Long userId, LocalDateTime start, LocalDateTime end);
}
