package depromeet.lessonfour.server.activityrecord.infra.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import depromeet.lessonfour.server.activityrecord.app.repository.ActivityRecordRepository;
import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ActivityRecordRepositoryImpl implements ActivityRecordRepository {

  private final JpaActivityRecordRepository jpaActivityRecordRepository;
  private final QueryDslActivityRecordRepository queryDslActivityRecordRepository;

  @Override
  public Optional<ActivityRecord> findByUserIdAndOccurredAt(Long userId, LocalDate date) {
    return queryDslActivityRecordRepository.findByUserIdAndOccurredAt(userId, date);
  }

  @Override
  public void save(ActivityRecord activityRecord) {
    jpaActivityRecordRepository.save(activityRecord);
  }

  @Override
  public boolean existsByUserIdAndActivityAt(Long userId, LocalDate activityAt) {
    return queryDslActivityRecordRepository.existsByUserIdAndActivityAt(userId, activityAt);
  }

  @Override
  public Optional<ActivityRecord> findByUser_IdAndIdAndIsDeletedFalse(
      Long userId, Long activityRecordId) {
    return jpaActivityRecordRepository.findByUser_IdAndIdAndIsDeletedFalse(
        userId, activityRecordId);
  }
}
