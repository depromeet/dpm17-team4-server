package depromeet.lessonfour.server.report.app.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import depromeet.lessonfour.server.report.domain.service.ActivityEvaluationService;
import depromeet.lessonfour.server.report.domain.vo.ActivityReport;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivityReportService {

  private final ActivityRecordService activityRecordService;
  private final ActivityEvaluationService activityEvaluationService;

  public ActivityReport generateDailyReport(Long userId, LocalDateTime baseDatetime) {
    LocalDate baseDate = baseDatetime.toLocalDate();
    Map<LocalDate, ActivityRecord> recordsByDate =
        activityRecordService.getActivityRecordsBetween(
            userId, baseDate.minusDays(1).atStartOfDay(), baseDate.atTime(LocalTime.MAX));

    ActivityRecord currentRecord = recordsByDate.get(baseDate);
    ActivityRecord previousRecord = recordsByDate.get(baseDate.minusDays(1));

    return activityEvaluationService.evaluate(previousRecord, currentRecord);
  }
}
