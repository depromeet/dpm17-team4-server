package depromeet.lessonfour.server.recordquery.infra;

import java.util.List;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.activityrecord.app.service.ActivityRecordQueryService;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.common.domain.vo.DailyExistence;
import depromeet.lessonfour.server.recordquery.app.service.ActivityRecordExistenceClient;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ActivityRecordExistenceService implements ActivityRecordExistenceClient {

  private final ActivityRecordQueryService activityRecordQueryService;

  @Override
  public List<DailyExistence> existsByActivityAtBetween(
      Long userId, ActivityAt start, ActivityAt end) {
    return activityRecordQueryService.existsByActivityAt(userId, start, end);
  }
}
