package depromeet.lessonfour.server.report.domain.vo.activity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import lombok.Getter;

@Getter
public class WeeklyActivityReport {

  /** 해당 주(월~일)의 DailyActivityReport 리스트 (항상 7개, 기록 없으면 빈 DailyActivityReport) */
  private final List<DailyActivityReport> dailyReports = new ArrayList<>();

  public static WeeklyActivityReport evaluateWeekly(
      List<ActivityRecord> records, ActivityAt startAt) {
    LocalDate startDate = startAt.toDate();
    Map<LocalDate, ActivityRecord> recordByDate =
        records.stream()
            .collect(
                Collectors.toMap(
                    r -> r.getActivityAt().toDate(),
                    Function.identity(),
                    (existing, replacement) -> replacement));
    WeeklyActivityReport weeklyActivityReport = new WeeklyActivityReport();
    for (int i = 0; i < 7; i++) {
      LocalDate currentDate = startDate.plusDays(i);
      ActivityRecord record = recordByDate.get(currentDate);
      DailyActivityReport dailyReport =
          (record == null)
              ? DailyActivityReport.empty()
              : DailyActivityReport.evaluate(null, record);
      weeklyActivityReport.dailyReports.add(dailyReport);
    }

    return weeklyActivityReport;
  }

  public int size() {
    return dailyReports.size();
  }

  /** 이 주에 “위험한 음식”이 포함된 날의 수 (대략적인 위험도 척도용) */
  public long dangerousFoodDays() {
    return dailyReports.stream().filter(DailyActivityReport::hasDangerousFood).count();
  }
}
