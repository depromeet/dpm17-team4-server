package depromeet.lessonfour.server.toiletrecord.app.service;

import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ToiletRecordQueryService {

  public ToiletRecord findByUserIdAndActivityAt(Long userId, LocalDateTime activityAt) {
    return null;
  }

  public List<ToiletRecord> findByUserAndPeriod(Long userId, LocalDateTime start, LocalDateTime end) {
    return List.of();
  }
}
