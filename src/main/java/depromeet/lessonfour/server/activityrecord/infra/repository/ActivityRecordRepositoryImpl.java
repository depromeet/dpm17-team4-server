package depromeet.lessonfour.server.activityrecord.infra.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import depromeet.lessonfour.server.activityrecord.domain.repository.ActivityRecordRepository;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.common.domain.vo.DailyExistence;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ActivityRecordRepositoryImpl implements ActivityRecordRepository {

  private final JpaActivityRecordRepository jpa;
  private final ActivityRecordQuery query;

  @Override
  public void save(ActivityRecord activityRecord) {
    jpa.save(activityRecord);
  }

  @Override
  public Optional<ActivityRecord> findById(Long activityRecordId) {
    return jpa.findByIdAndIsDeletedFalse(activityRecordId);
  }

  @Override
  public Optional<ActivityRecord> findByActivityAt(Long userId, ActivityAt date) {
    return query.findByDate(userId, date);
  }

  @Override
  public List<ActivityRecord> findByActivityAtBetween(
      Long userId, ActivityAt start, ActivityAt end) {
    return query.findByActivityAtBetween(userId, start, end);
  }

  @Override
  public List<DailyExistence> existsByActivityAt(Long userId, ActivityAt start, ActivityAt end) {
    return query.findDailyExistencesByActivityAt(userId, start, end);
  }
}
