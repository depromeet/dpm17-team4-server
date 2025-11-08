package depromeet.lessonfour.server.toiletrecord.app.service;

import java.util.List;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.common.domain.vo.DailyExistence;
import depromeet.lessonfour.server.common.domain.vo.RecordTime;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import depromeet.lessonfour.server.toiletrecord.domain.repository.ToiletRecordRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ToiletRecordQueryService {

  private final ToiletRecordRepository toiletRecordRepository;

  public List<ToiletRecord> findAllByActivityAt(Long userId, ActivityAt activityAt) {
    return toiletRecordRepository.findAllByActivityAt(userId, activityAt);
  }

  public List<RecordTime> findTimesByDate(Long userId, ActivityAt at) {
    return toiletRecordRepository.findTimesByDate(userId, at);
  }

  public List<DailyExistence> findDailyExistencesBetween(
      Long userId, ActivityAt startInclude, ActivityAt endInclude) {
    return toiletRecordRepository.findDailyExistencesBetween(userId, startInclude, endInclude);
  }

  public int countByActivityAt(Long userId, ActivityAt at) {
    return toiletRecordRepository.countByActivityAt(userId, at);
  }

  public List<ToiletRecord> findByActivityAtBetween(
      Long userId, ActivityAt startAt, ActivityAt endAt) {
    return toiletRecordRepository.findAllByActivityAtBetween(userId, startAt, endAt);
  }
}
