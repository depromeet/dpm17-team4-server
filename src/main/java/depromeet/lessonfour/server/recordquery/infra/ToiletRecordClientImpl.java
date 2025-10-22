package depromeet.lessonfour.server.recordquery.infra;

import java.util.List;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.common.domain.vo.DailyExistence;
import depromeet.lessonfour.server.recordquery.app.client.ToiletRecordClient;
import depromeet.lessonfour.server.toiletrecord.app.service.ToiletRecordQueryService;
import lombok.RequiredArgsConstructor;

@Component("recordQueryToiletRecordClient")
@RequiredArgsConstructor
public class ToiletRecordClientImpl implements ToiletRecordClient {

  private final ToiletRecordQueryService toiletRecordQueryService;

  @Override
  public List<DailyExistence> existsByActivityAtBetween(
      Long userId, ActivityAt start, ActivityAt end) {
    return toiletRecordQueryService.existsByActivityAt(userId, start, end);
  }

  @Override
  public int getToiletRecordCountByActivityAt(Long userId, ActivityAt activityAt) {
    return toiletRecordQueryService.countByActivityAt(userId, activityAt);
  }
}
