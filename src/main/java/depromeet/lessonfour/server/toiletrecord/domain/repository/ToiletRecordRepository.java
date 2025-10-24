package depromeet.lessonfour.server.toiletrecord.domain.repository;

import java.util.List;
import java.util.Optional;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.common.domain.vo.DailyExistence;
import depromeet.lessonfour.server.common.domain.vo.RecordTime;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;

public interface ToiletRecordRepository {

  void save(ToiletRecord record);

  Optional<ToiletRecord> findById(Long userId, Long recordId);

  List<ToiletRecord> findByDate(Long userId, ActivityAt at);

  List<RecordTime> findTimesByDate(Long userId, ActivityAt activityAt);

  List<ToiletRecord> findAllByActivityAt(Long userId, ActivityAt at);

  List<DailyExistence> findDailyExistencesBetween(Long userId, ActivityAt start, ActivityAt end);

  int countByActivityAt(Long userId, ActivityAt at);
}
