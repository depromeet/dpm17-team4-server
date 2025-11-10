package depromeet.lessonfour.server.report.domain.vo.activity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import lombok.Getter;

@Getter
public class WeeklyActivityReport {

  /** 해당 주(월~일)의 DailyActivityReport 리스트 (최대 7개, 기록 없으면 빈 DailyActivityReport 일 수도 있음) */
  private final List<DailyActivityReport> dailyReports = new ArrayList<>();

  public static WeeklyActivityReport evaluateWeekly(List<ActivityRecord> records) {
    // 날짜 오름차순 정렬 (뷰에서 월~일 순서대로 사용하기 좋게)
    List<DailyActivityReport> sortedDailyReports =
        records.stream()
            .sorted(
                Comparator.comparing(
                    r -> r.getActivityAt().toDate() // ActivityAt -> LocalDate
                    ))
            .map(activityRecord -> DailyActivityReport.evaluate(null, activityRecord))
            .toList();

    WeeklyActivityReport weeklyActivityReport = new WeeklyActivityReport();
    weeklyActivityReport.dailyReports.addAll(sortedDailyReports);

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
