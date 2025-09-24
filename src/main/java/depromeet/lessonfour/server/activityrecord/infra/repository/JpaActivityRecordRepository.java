package depromeet.lessonfour.server.activityrecord.infra.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;

public interface JpaActivityRecordRepository extends JpaRepository<ActivityRecord, Long> {

  Optional<ActivityRecord> findByUser_IdAndIdAndIsDeletedFalse(Long userId, Long activityRecordId);
}
