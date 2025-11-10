package depromeet.lessonfour.server.report.app.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.report.app.client.ActivityRecordClient;
import depromeet.lessonfour.server.report.domain.vo.activity.DailyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.activity.MonthlyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.activity.WeeklyActivityReport;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivityReportService {

  private final ActivityRecordClient activityRecordClient;

  /** 일간 생활 기록 리포트 생성 */
  public DailyActivityReport generateDailyReport(Long userId, LocalDateTime baseDatetime) {
    LocalDate baseDate = baseDatetime.toLocalDate();
    Map<LocalDate, ActivityRecord> recordsByDate =
        activityRecordClient.getActivityRecordsBetween(
            userId, baseDate.minusDays(1).atStartOfDay(), baseDate.plusDays(1).atStartOfDay());

    ActivityRecord currentRecord = recordsByDate.get(baseDate);
    ActivityRecord previousRecord = recordsByDate.get(baseDate.minusDays(1));

    return DailyActivityReport.evaluate(previousRecord, currentRecord);
  }

  /** 주간 생활 기록 리포트 생성 */
  public WeeklyActivityReport generateWeeklyReport(
      Long userId, ActivityAt startAt, ActivityAt endAt) {

    List<ActivityRecord> records =
        activityRecordClient.getActivityRecordsBetween(userId, startAt, endAt);

    return WeeklyActivityReport.evaluateWeekly(records);
  }

  /** 월간 생활 기록 리포트 생성 */
  public MonthlyActivityReport generateMonthlyReport(
      Long userId, ActivityAt startAt, ActivityAt endAt) {
    ActivityAt lastMonthStart = startAt.getLastMonth();
    LocalDate monthFirstDay = startAt.toDate();

    // 해당 달 기록
    List<ActivityRecord> current =
        activityRecordClient.getActivityRecordsBetween(userId, startAt, endAt);

    // 지난 달 기록
    List<ActivityRecord> last =
        activityRecordClient.getActivityRecordsBetween(userId, lastMonthStart, startAt);

    return MonthlyActivityReport.evaluateMonthly(current, last, monthFirstDay);
  }
}
