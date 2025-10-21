package depromeet.lessonfour.server.recordquery.infra;

import java.util.List;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.activityrecord.app.service.ActivityRecordQueryService;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.common.domain.vo.DailyExistence;
import depromeet.lessonfour.server.recordquery.app.client.ActivityRecordClient;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ActivityRecordService implements ActivityRecordClient {

  private final ActivityRecordQueryService activityRecordQueryService;

  @Override
  public List<DailyExistence> existsByActivityAtBetween(
      Long userId, ActivityAt start, ActivityAt end) {
    return activityRecordQueryService.existsByActivityAt(userId, start, end);
  }

  @Override
  public int getActivityRecordCountByActivityAt(Long userId, ActivityAt activityAt) {
    return activityRecordQueryService.countByActivityAt(userId, activityAt);
  }
}
