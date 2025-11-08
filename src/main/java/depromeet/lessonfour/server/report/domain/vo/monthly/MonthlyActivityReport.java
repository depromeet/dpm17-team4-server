package depromeet.lessonfour.server.report.domain.vo.monthly;

import java.time.LocalDate;
import java.util.List;

import depromeet.lessonfour.server.report.domain.vo.DailyActivityReport;

public record MonthlyActivityReport(
    int size,
    List<WeeklyGroup> weeklyGroups,
    int dangerousFoodDays, // 이 달 전체 'dangerous=true' 일수
    int lastMonthDangerousDays // 지난달 dangerous 일수 (food 섹션 비교용)
    ) {

  public static record WeeklyGroup(
      int weekIndex, // 1~5
      LocalDate startDate, // yyyy-MM-dd
      LocalDate endDate, // yyyy-MM-dd
      List<DailyActivityReport> dailyReports) {}

  public static MonthlyActivityReport empty(int lastMonthDangerousDays) {
    return new MonthlyActivityReport(0, List.of(), 0, lastMonthDangerousDays);
  }
}
