package depromeet.lessonfour.server.report.infra;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.activityrecord.app.service.ActivityRecordQueryService;
import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.report.app.client.ActivityRecordClient;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ActivityRecordClientImpl implements ActivityRecordClient {

  private final ActivityRecordQueryService activityRecordQueryService;

  @Override
  public Map<LocalDate, ActivityRecord> getActivityRecordsBetween(
      Long userId, LocalDateTime start, LocalDateTime end) {
    ActivityAt from = ActivityAt.from(start);
    ActivityAt to = ActivityAt.from(end);

    List<ActivityRecord> records =
        activityRecordQueryService.getActivityRecordsBetween(userId, from, to);
    return groupByDate(records);
  }

  private static Map<LocalDate, ActivityRecord> groupByDate(List<ActivityRecord> activityRecords) {
    return activityRecords.stream()
        .collect(
            Collectors.toMap(
                record -> record.getActivityAt().toDate(),
                Function.identity(),
                (existing, replacement) -> existing));
  }

  @Override
  public List<ActivityRecord> getActivityRecordsBetween(
      Long userId, ActivityAt startAt, ActivityAt endAt) {
    return activityRecordQueryService.getActivityRecordsBetween(userId, startAt, endAt);
  }
}
