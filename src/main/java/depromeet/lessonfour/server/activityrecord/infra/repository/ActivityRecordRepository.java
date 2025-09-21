package depromeet.lessonfour.server.activityrecord.infra.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;

public interface ActivityRecordRepository extends JpaRepository<ActivityRecord, Long> {

  boolean existsByUser_IdAndActivityAtBetweenAndIsDeletedFalse(
      Long userId, LocalDateTime startOfDay, LocalDateTime endOfDay);

  Optional<ActivityRecord> findByUser_IdAndIdAndIsDeletedFalse(Long userId, Long activityRecordId);
}
