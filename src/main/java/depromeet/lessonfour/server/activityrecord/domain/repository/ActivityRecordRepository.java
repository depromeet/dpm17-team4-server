package depromeet.lessonfour.server.activityrecord.domain.repository;

import java.util.List;
import java.util.Optional;

import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.common.domain.vo.DailyExistence;

public interface ActivityRecordRepository {

  Optional<ActivityRecord> findByActivityAt(Long userId, ActivityAt date);

  Optional<ActivityRecord> findById(Long activityRecordId);

  void save(ActivityRecord activityRecord);

  List<ActivityRecord> findByActivityAtBetween(Long userId, ActivityAt start, ActivityAt end);

  DailyExistence existsByActivityAt(Long userId, ActivityAt activityAt);

  List<DailyExistence> existsByActivityAt(Long userId, ActivityAt start, ActivityAt end);
}
