package depromeet.lessonfour.server.activityrecord.domain.repository;

import java.util.List;
import java.util.Optional;

import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import depromeet.lessonfour.server.common.domain.view.DailyExistenceView;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;

public interface ActivityRecordRepository {

  Optional<ActivityRecord> findByActivityAt(Long userId, ActivityAt date);

  Optional<ActivityRecord> findById(Long activityRecordId);

  void save(ActivityRecord activityRecord);

  List<ActivityRecord> findByActivityAtBetween(Long userId, ActivityAt start, ActivityAt end);

  DailyExistenceView existsByActivityAt(Long userId, ActivityAt activityAt);

  List<DailyExistenceView> existsByActivityAt(Long userId, ActivityAt start, ActivityAt end);
}
