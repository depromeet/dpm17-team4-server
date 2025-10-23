package depromeet.lessonfour.server.recordquery.app.service;

import java.util.List;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.common.domain.vo.RecordTime;

public interface ToiletRecordTimesClient {
	List<RecordTime> findTimesByDate(Long userId, ActivityAt date);
}