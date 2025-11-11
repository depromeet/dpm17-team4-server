package depromeet.lessonfour.server.report.infra;

import java.util.List;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.activityrecord.app.service.ActivityRecordQueryService;
import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.report.app.client.ActivityRecordClient;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ActivityRecordClientImpl implements ActivityRecordClient {

  private final ActivityRecordQueryService activityRecordQueryService;

  @Override
  public List<ActivityRecord> getActivityRecordsBetween(
      Long userId, ActivityAt startAt, ActivityAt endAt) {
    return activityRecordQueryService.getActivityRecordsBetween(userId, startAt, endAt);
  }
}
