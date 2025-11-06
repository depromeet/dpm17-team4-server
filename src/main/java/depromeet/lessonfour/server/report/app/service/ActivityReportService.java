package depromeet.lessonfour.server.report.app.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import depromeet.lessonfour.server.report.app.client.ActivityRecordClient;
import depromeet.lessonfour.server.report.domain.service.ActivityEvaluationService;
import depromeet.lessonfour.server.report.domain.vo.DailyActivityReport;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivityReportService {

  private final ActivityRecordClient activityRecordClient;
  private final ActivityEvaluationService activityEvaluationService;

  public DailyActivityReport generateDailyReport(Long userId, LocalDateTime baseDatetime) {
    LocalDate baseDate = baseDatetime.toLocalDate();
    Map<LocalDate, ActivityRecord> recordsByDate =
        activityRecordClient.getActivityRecordsBetween(
            userId, baseDate.minusDays(1).atStartOfDay(), baseDate.plusDays(1).atStartOfDay());

    ActivityRecord currentRecord = recordsByDate.get(baseDate);
    ActivityRecord previousRecord = recordsByDate.get(baseDate.minusDays(1));

    return activityEvaluationService.evaluate(previousRecord, currentRecord);
  }
}
