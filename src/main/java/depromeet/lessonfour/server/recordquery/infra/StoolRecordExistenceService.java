package depromeet.lessonfour.server.recordquery.infra;

import java.util.List;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.common.domain.view.DailyExistenceView;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.recordquery.app.service.StoolRecordExistenceClient;
import depromeet.lessonfour.server.toiletrecord.app.service.ToiletRecordQueryService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StoolRecordExistenceService implements StoolRecordExistenceClient {

  private final ToiletRecordQueryService toiletRecordQueryService;

  @Override
  public List<DailyExistenceView> existsByActivityAtBetween(
      Long userId, ActivityAt start, ActivityAt end) {
    return toiletRecordQueryService.existsByActivityAt(userId, start, end);
  }
}
