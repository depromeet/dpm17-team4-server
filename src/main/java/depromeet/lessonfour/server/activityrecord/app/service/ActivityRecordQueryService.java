package depromeet.lessonfour.server.activityrecord.app.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.activityrecord.app.repository.ActivityRecordRepository;
import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivityRecordQueryService {

  private final ActivityRecordRepository activityRecordRepository;

  public Map<LocalDate, ActivityRecord> findDayAndDayBefore(Long userId, LocalDateTime dateTime) {
    return activityRecordRepository.findDayAndDayBefore(userId, dateTime.toLocalDate());
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
