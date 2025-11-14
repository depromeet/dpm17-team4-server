package depromeet.lessonfour.server.report.app.service;

import java.time.LocalDate;
import java.util.List;

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

  public DailyActivityReport generateDailyReport(Long userId, ActivityAt activityAt) {
    List<ActivityRecord> records =
        activityRecordClient.getActivityRecordsBetween(
            userId, activityAt.getDayBefore(), activityAt.getDayAfter());

    ActivityRecord yesterday =
        records.stream()
            .filter(record -> record.getActivityAt().isDayBefore(activityAt))
            .findFirst()
            .orElse(null);

    ActivityRecord today =
        records.stream()
            .filter(record -> record.getActivityAt().isSameDate(activityAt))
            .findFirst()
            .orElse(null);

    return DailyActivityReport.evaluate(yesterday, today);
  }

  /** 주간 생활 기록 리포트 생성 */
  public WeeklyActivityReport generateWeeklyReport(
      Long userId, ActivityAt startAt, ActivityAt endAt) {

    List<ActivityRecord> records =
        activityRecordClient.getActivityRecordsBetween(userId, startAt, endAt);

    return WeeklyActivityReport.evaluateWeekly(records, startAt);
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
