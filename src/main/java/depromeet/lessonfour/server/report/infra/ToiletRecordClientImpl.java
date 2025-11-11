package depromeet.lessonfour.server.report.infra;

import java.util.List;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.report.app.client.ToiletRecordClient;
import depromeet.lessonfour.server.toiletrecord.app.service.ToiletRecordQueryService;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import lombok.RequiredArgsConstructor;

@Component("reportToiletRecordClient")
@RequiredArgsConstructor
public class ToiletRecordClientImpl implements ToiletRecordClient {

  private final ToiletRecordQueryService toiletRecordQueryService;

  @Override
  public List<ToiletRecord> getToiletRecordsByActivityAt(Long userId, ActivityAt activityAt) {
    return toiletRecordQueryService.findAllByActivityAt(userId, activityAt);
  }

  @Override
  public List<ToiletRecord> getToiletRecordsByActivityAtBetween(
      Long userId, ActivityAt startAt, ActivityAt endAt) {
    return toiletRecordQueryService.findByActivityAtBetween(userId, startAt, endAt);
  }
}
