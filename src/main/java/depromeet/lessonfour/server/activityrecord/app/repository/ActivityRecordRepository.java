package depromeet.lessonfour.server.activityrecord.app.repository;

import java.time.LocalDateTime;

import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;

public interface ActivityRecordRepository {

  void save(ActivityRecord activityRecord);

  boolean existsByUserIdAndActivityAt(Long userId, LocalDateTime activityAt);
}
