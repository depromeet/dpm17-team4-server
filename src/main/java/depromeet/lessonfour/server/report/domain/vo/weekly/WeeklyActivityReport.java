package depromeet.lessonfour.server.report.domain.vo.weekly;

import java.util.List;

import depromeet.lessonfour.server.report.domain.vo.DailyActivityReport;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WeeklyActivityReport {

  /** 해당 주(월~일)의 DailyActivityReport 리스트 (최대 7개, 기록 없으면 빈 DailyActivityReport 일 수도 있음) */
  private final List<DailyActivityReport> dailyReports;

  public int size() {
    return dailyReports != null ? dailyReports.size() : 0;
  }

  /** 이 주에 “위험한 음식”이 포함된 날의 수 (대략적인 위험도 척도용) */
  public long dangerousFoodDays() {
    if (dailyReports == null) {
      return 0;
    }
    return dailyReports.stream().filter(DailyActivityReport::hasDangerousFood).count();
  }

  /** 이 주에 스트레스가 높았던 날의 수 */
  public long highStressDays() {
    if (dailyReports == null) {
      return 0;
    }
    return dailyReports.stream().filter(DailyActivityReport::hasStress).count();
  }
}
