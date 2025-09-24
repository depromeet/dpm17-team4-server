package depromeet.lessonfour.server.toiletrecord.app.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.toiletrecord.app.repository.ToiletRecordRepository;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ToiletRecordQueryService {

  private final ToiletRecordRepository toiletRecordRepository;

  public List<ToiletRecord> findByDate(Long userId, LocalDate date) {
    return toiletRecordRepository.findByDate(userId, date);
  }
}
