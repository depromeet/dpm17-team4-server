package depromeet.lessonfour.server.activityrecord.infra.repository;

import java.time.LocalDate;
import java.util.List;
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
  public Optional<ActivityRecord> findByUserAndDate(Long userId, LocalDate date) {
    return queryDslActivityRecordRepository.findByUserIdAndOccurredAt(userId, date);
  }

  @Override
  public void save(ActivityRecord activityRecord) {
    jpaActivityRecordRepository.save(activityRecord);
  }

  @Override
  public boolean existsByUserAndDate(Long userId, LocalDate activityAt) {
    return queryDslActivityRecordRepository.existsByUserIdAndActivityAt(userId, activityAt);
  }

  @Override
  public Optional<ActivityRecord> findByUserAndId(Long userId, Long activityRecordId) {
    return jpaActivityRecordRepository.findByUser_IdAndIdAndIsDeletedFalse(
        userId, activityRecordId);
  }

  @Override
  public List<ActivityRecord> findDayAndDayBefore(Long userId, LocalDate day) {
    return queryDslActivityRecordRepository.findDayAndDayBefore(userId, day);
  }
}
