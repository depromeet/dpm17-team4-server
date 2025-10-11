package depromeet.lessonfour.server.recordquery.app.service;

import java.util.List;

import depromeet.lessonfour.server.common.domain.view.DailyExistenceView;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;

public interface StoolRecordExistenceClient {
  List<DailyExistenceView> existsByActivityAtBetween(Long userId, ActivityAt start, ActivityAt end);
}
