package depromeet.lessonfour.server.activityrecords.adapters;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

import depromeet.lessonfour.server.activityrecords.domain.entities.ActivityRecord;

public interface ActivityRecordRepository extends JpaRepository<ActivityRecord, Long> {

  boolean existsByUser_IdAndCreatedAtBetween(
      Long id, LocalDateTime startOfDay, LocalDateTime endOfDay);
}
