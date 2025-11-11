package depromeet.lessonfour.server.report.app.client;

import java.util.List;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;

public interface ToiletRecordClient {

  List<ToiletRecord> getToiletRecordsByActivityAt(Long userId, ActivityAt activityAt);

  List<ToiletRecord> getToiletRecordsByActivityAtBetween(
      Long userId, ActivityAt startAt, ActivityAt endAt);
}
