package depromeet.lessonfour.server.recordquery.app.service;

import java.util.List;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.common.domain.vo.DailyExistence;

public interface ToiletRecordExistenceClient {
  List<DailyExistence> existsByActivityAtBetween(Long userId, ActivityAt start, ActivityAt end);
}
