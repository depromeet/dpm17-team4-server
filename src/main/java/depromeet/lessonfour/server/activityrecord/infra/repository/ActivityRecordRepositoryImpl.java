package depromeet.lessonfour.server.activityrecord.infra.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import depromeet.lessonfour.server.activityrecord.domain.repository.ActivityRecordRepository;
import depromeet.lessonfour.server.activityrecord.domain.vo.ActivityAt;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ActivityRecordRepositoryImpl implements ActivityRecordRepository {

  private final JpaActivityRecordRepository jpaActivityRecordRepository;
  private final QueryDslActivityRecordRepository queryDslActivityRecordRepository;

  @Override
  public void save(ActivityRecord activityRecord) {
    jpaActivityRecordRepository.save(activityRecord);
  }

  @Override
  public Optional<ActivityRecord> findById(Long activityRecordId) {
    return jpaActivityRecordRepository.findByIdAndIsDeletedFalse(activityRecordId);
  }

  @Override
  public Optional<ActivityRecord> findByActivityAt(Long userId, ActivityAt date) {
    return queryDslActivityRecordRepository.findByDate(userId, date);
  }

  @Override
  public List<ActivityRecord> findByActivityAtBetween(
      Long userId, ActivityAt start, ActivityAt end) {
    return queryDslActivityRecordRepository.findByActivityAtBetween(userId, start, end);
  }
}
