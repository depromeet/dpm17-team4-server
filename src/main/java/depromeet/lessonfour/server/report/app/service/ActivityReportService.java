package depromeet.lessonfour.server.report.app.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.activityrecord.app.service.ActivityRecordQueryService;
import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import depromeet.lessonfour.server.report.domain.service.ActivityEvaluationService;
import depromeet.lessonfour.server.report.domain.vo.ActivityReport;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivityReportService {

  private final ActivityRecordQueryService activityRecordQueryService;
  private final ActivityEvaluationService activityEvaluationService;

  public ActivityReport generateDailyReport(Long userId, LocalDateTime baseDatetime) {
    Map<LocalDate, ActivityRecord> recordsByDate =
        activityRecordQueryService.findDayAndDayBefore(userId, baseDatetime);

    LocalDate baseDate = baseDatetime.toLocalDate();
    ActivityRecord currentRecord = recordsByDate.get(baseDate);
    ActivityRecord previousRecord = recordsByDate.get(baseDate.minusDays(1));

    return activityEvaluationService.evaluate(previousRecord, currentRecord);
  }
}
