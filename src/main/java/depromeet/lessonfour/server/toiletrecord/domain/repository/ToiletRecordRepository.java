package depromeet.lessonfour.server.toiletrecord.domain.repository;

import java.util.List;
import java.util.Optional;

import depromeet.lessonfour.server.common.domain.view.DailyExistenceView;
import depromeet.lessonfour.server.common.domain.view.RecordTimeView;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;

public interface ToiletRecordRepository {

  void save(ToiletRecord record);

  Optional<ToiletRecord> findByUserAndId(Long userId, Long recordId);

  List<ToiletRecord> findByDate(Long userId, ActivityAt at);

  List<RecordTimeView> findTimesByDate(Long userId, ActivityAt activityAt);

  DailyExistenceView existsByActivityAt(Long userId, ActivityAt at);

  List<DailyExistenceView> existsByActivityAt(Long userId, ActivityAt start, ActivityAt end);
}
