package depromeet.lessonfour.server.recordquery.infra;

import java.util.List;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.common.domain.vo.DailyExistence;
import depromeet.lessonfour.server.recordquery.app.service.ToiletRecordExistenceClient;
import depromeet.lessonfour.server.toiletrecord.app.service.ToiletRecordQueryService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ToiletRecordExistenceService implements ToiletRecordExistenceClient {

  private final ToiletRecordQueryService toiletRecordQueryService;

  @Override
  public List<DailyExistence> existsByActivityAtBetween(
      Long userId, ActivityAt start, ActivityAt end) {
    return toiletRecordQueryService.existsByActivityAt(userId, start, end);
  }
}
