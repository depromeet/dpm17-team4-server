package depromeet.lessonfour.server.activityrecord.app.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import depromeet.lessonfour.server.activityrecord.domain.repository.ActivityRecordRepository;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.common.domain.vo.DailyExistence;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ActivityRecordQueryService {

  private final ActivityRecordRepository activityRecordRepository;

  public List<ActivityRecord> getActivityRecordsBetween(
      Long userId, ActivityAt start, ActivityAt end) {

    return activityRecordRepository.findAllByActivityAtBetween(userId, start, end);
  }

  public List<DailyExistence> findDailyExistencesBetween(
      Long userId, ActivityAt startInclude, ActivityAt endInclude) {
    return activityRecordRepository.findDailyExistencesBetween(userId, startInclude, endInclude);
  }

  public boolean existsByActivityAt(Long userId, ActivityAt activityAt) {
    return activityRecordRepository.existsByActivityAt(userId, activityAt);
  }
}
