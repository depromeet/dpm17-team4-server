package depromeet.lessonfour.server.activityrecord.app.service;

import depromeet.lessonfour.server.activityrecord.app.repository.ActivityRecordRepository;
import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ActivityRecordQueryService {

  private final ActivityRecordRepository activityRecordRepository;

  public Map<LocalDate, ActivityRecord> findDayAndDayBefore(Long userId, LocalDateTime dateTime) {
    List<ActivityRecord> records =
        activityRecordRepository.findDayAndDayBefore(userId, dateTime.toLocalDate());

    return groupByDate(records);
  }

  private static Map<LocalDate, ActivityRecord> groupByDate(List<ActivityRecord> activityRecords) {
    return activityRecords.stream()
        .collect(
            Collectors.toMap(
                record -> LocalDate.from(record.getActivityAt()),
                Function.identity(),
                (existing, replacement) -> existing));
  }
}
