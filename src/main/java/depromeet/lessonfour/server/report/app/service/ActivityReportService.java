package depromeet.lessonfour.server.report.app.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.report.app.client.ActivityRecordClient;
import depromeet.lessonfour.server.report.domain.service.ActivityEvaluationService;
import depromeet.lessonfour.server.report.domain.vo.DailyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.monthly.MonthlyActivityReport;
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

  // TODO : 다른 통계치 추가하기
  public MonthlyActivityReport generateMonthlyReport(
      Long userId, ActivityAt startAt, ActivityAt endAt) {
    List<ActivityRecord> records =
        activityRecordClient.getActivityRecordsBetween(userId, startAt, endAt);
    return MonthlyActivityReport.dummy();
  }
}
