package depromeet.lessonfour.server.activityrecord.app.service;

import java.time.LocalDateTime;
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
      Long userId, LocalDateTime start, LocalDateTime end) {

    ActivityAt from = ActivityAt.from(start);
    ActivityAt to = ActivityAt.from(end);

    return activityRecordRepository.findByActivityAtBetween(userId, from, to);
  }

  public DailyExistence existsByActivityAt(Long userId, ActivityAt at) {
    return activityRecordRepository.existsByActivityAt(userId, at);
  }

  public List<DailyExistence> existsByActivityAt(Long userId, ActivityAt start, ActivityAt end) {
    return activityRecordRepository.existsByActivityAt(userId, start, end);
  }
}
