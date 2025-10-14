package depromeet.lessonfour.server.recordquery.app.service;

import java.util.List;

import depromeet.lessonfour.server.common.domain.view.RecordTimeView;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;

public interface ToiletRecordTimesClient {
  List<RecordTimeView> findTimesByDate(Long userId, ActivityAt date);
}
