package depromeet.lessonfour.server.report.app.client;

import java.time.LocalDate;
import java.util.List;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;

public interface ToiletRecordClient {

  List<ToiletRecord> getToiletRecordsByDate(Long userId, LocalDate date);

  List<ToiletRecord> getToiletRecordsByActivityAtBetween(
      Long userId, ActivityAt startAt, ActivityAt endAt);
}
