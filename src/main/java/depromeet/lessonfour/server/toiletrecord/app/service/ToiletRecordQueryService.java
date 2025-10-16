package depromeet.lessonfour.server.toiletrecord.app.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.common.domain.view.DailyExistenceView;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import depromeet.lessonfour.server.toiletrecord.domain.repository.ToiletRecordRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ToiletRecordQueryService {

  private final ToiletRecordRepository toiletRecordRepository;

  public List<ToiletRecord> findByDate(Long userId, LocalDate date) {
    return toiletRecordRepository.findByActivityAt(userId, ActivityAt.of(date));
  }

  public DailyExistenceView existsByActivityAt(Long userId, ActivityAt at) {
    return toiletRecordRepository.existsByActivityAt(userId, at);
  }

  public List<DailyExistenceView> existsByActivityAt(
      Long userId, ActivityAt start, ActivityAt end) {
    return toiletRecordRepository.existsByActivityAt(userId, start, end);
  }
}
