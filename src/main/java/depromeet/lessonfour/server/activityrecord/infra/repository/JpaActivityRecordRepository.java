package depromeet.lessonfour.server.activityrecord.infra.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;

public interface JpaActivityRecordRepository extends JpaRepository<ActivityRecord, Long> {

  Optional<ActivityRecord> findByIdAndIsDeletedFalse(Long activityRecordId);

  Optional<ActivityRecord> findByUserIdAndIdAndIsDeletedFalse(Long userId, Long activityRecordId);

  @Query(
      "SELECT ar FROM ActivityRecord ar LEFT JOIN FETCH ar.foodRecords WHERE ar.id = :id AND ar.isDeleted = false")
  Optional<ActivityRecord> findByIdWithFoodRecords(@Param("id") Long id);
}
