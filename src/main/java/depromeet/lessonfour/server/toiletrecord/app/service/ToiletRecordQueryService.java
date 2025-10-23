package depromeet.lessonfour.server.toiletrecord.app.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.common.domain.vo.DailyExistence;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import depromeet.lessonfour.server.toiletrecord.domain.repository.ToiletRecordRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ToiletRecordQueryService {

  private final ToiletRecordRepository toiletRecordRepository;

  public List<ToiletRecord> findByDate(Long userId, LocalDate date) {
    return toiletRecordRepository.findAllByActivityAt(userId, ActivityAt.of(date));
  }

  public List<DailyExistence> findDailyExistencesBetween(
      Long userId, ActivityAt startInclude, ActivityAt endInclude) {
    return toiletRecordRepository.findDailyExistencesBetween(userId, startInclude, endInclude);
  }

  public int countByActivityAt(Long userId, ActivityAt at) {
    return toiletRecordRepository.countByActivityAt(userId, at);
  }
}
