package depromeet.lessonfour.server.activityrecord.app.service;

import depromeet.lessonfour.server.activityrecord.app.repository.ActivityRecordRepository;
import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivityRecordQueryService {

  private final ActivityRecordRepository activityRecordRepository;

  public List<ActivityRecord> findByUserAndPeriod(Long userId, LocalDate start, LocalDate end) {
    return List.of();
  }
}
