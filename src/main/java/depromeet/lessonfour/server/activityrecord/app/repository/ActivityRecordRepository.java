package depromeet.lessonfour.server.activityrecord.app.repository;

import java.time.LocalDate;
import java.util.Optional;

import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;

public interface ActivityRecordRepository {

  Optional<ActivityRecord> findByUserAndDate(Long userId, LocalDate date);

  void save(ActivityRecord activityRecord);

  boolean existsByUserAndDate(Long userId, LocalDate activityAt);

  Optional<ActivityRecord> findByUserAndId(Long userId, Long activityRecordId);
}
