package depromeet.lessonfour.server.recordquery.infra;

import java.util.List;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.common.domain.view.RecordTimeView;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.recordquery.app.service.ToiletRecordTimesClient;
import depromeet.lessonfour.server.toiletrecord.app.service.ToiletRecordQueryService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ToiletRecordTimesService implements ToiletRecordTimesClient {

  private final ToiletRecordQueryService toiletRecordQueryService;

  @Override
  public List<RecordTimeView> findTimesByDate(Long userId, ActivityAt date) {
    return toiletRecordQueryService.findTimesByDate(userId, date);
  }
}
