package depromeet.lessonfour.server.activityrecord.app.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;

public interface ActivityRecordRepository {

  Optional<ActivityRecord> findByUserIdAndOccurredAt(Long userId, LocalDate date);

  void save(ActivityRecord activityRecord);

  boolean existsByUserIdAndActivityAt(Long userId, LocalDateTime activityAt);

  Optional<ActivityRecord> findByUser_IdAndIdAndIsDeletedFalse(Long userId, Long activityRecordId);
}
