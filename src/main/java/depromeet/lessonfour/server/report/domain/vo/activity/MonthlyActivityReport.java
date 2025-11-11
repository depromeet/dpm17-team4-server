package depromeet.lessonfour.server.report.domain.vo.activity;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;

public record MonthlyActivityReport(
    int lastMonthDangerousDays, // 지난달 dangerous 일수 (food 섹션 비교용)
    int dangerousFoodDays, // 이 달 전체 'dangerous=true' 일수
    List<WeeklyActivityReportGroup> weeklyActivityReportGroups) {

  public static MonthlyActivityReport evaluateMonthly(
      List<ActivityRecord> currentMonth, List<ActivityRecord> lastMonth, LocalDate monthFirstDay) {

    if (currentMonth.isEmpty()) {
      int lastMonthDangerousDays = countDangerousFoodDays(lastMonth);
      return MonthlyActivityReport.empty(lastMonthDangerousDays);
    }

    // 날짜 오름차순 정렬
    List<ActivityRecord> sortedRecords =
        currentMonth.stream()
            .sorted(Comparator.comparing(r -> r.getActivityAt().toDate()))
            .toList();

    // 월간 리포트를 주차별로 그룹핑: 1~7일(1주차), 8~14일(2주차), 15~21일(3주차), 22~28일(4주차), 29~말일(5주차)
    List<WeeklyActivityReportGroup> weeklyGroups = createWeeklyGroups(sortedRecords, monthFirstDay);

    // 이번 달과 지난 달의 위험 음식 섭취 일수 계산
    int currentMonthDangerousDays = countDangerousFoodDays(sortedRecords);
    int lastMonthDangerousDays = countDangerousFoodDays(lastMonth);

    return new MonthlyActivityReport(
        lastMonthDangerousDays, currentMonthDangerousDays, weeklyGroups);
  }

  /** 주차별 활동 기록 그룹 생성 월간 리포트를 5개 주차로 나눔: 1~7일, 8~14일, 15~21일, 22~28일, 29~말일 */
  private static List<WeeklyActivityReportGroup> createWeeklyGroups(
      List<ActivityRecord> sortedRecords, LocalDate monthFirstDay) {
    int[] weekEndDays = new int[] {7, 14, 21, 28, Integer.MAX_VALUE};
    int monthLength = YearMonth.from(monthFirstDay).lengthOfMonth();

    return IntStream.range(0, 5)
        .mapToObj(
            weekIndex -> {
              int weekStartDay = (weekIndex == 0) ? 1 : (weekEndDays[weekIndex - 1] + 1);

              // 주차 시작일이 월말을 넘어가면 빈 리스트 반환
              if (weekStartDay > monthLength) {
                return null;
              }

              int weekEndDay = Math.min(weekEndDays[weekIndex], monthLength);

              LocalDate weekStart = monthFirstDay.withDayOfMonth(weekStartDay);
              LocalDate weekEnd = monthFirstDay.withDayOfMonth(weekEndDay);

              // 해당 주차 범위 내의 활동 기록 필터링 및 일간 리포트 생성
              List<DailyActivityReport> dailyReports =
                  sortedRecords.stream()
                      .filter(
                          r -> {
                            LocalDate recordDate = r.getActivityAt().toDate();
                            return !recordDate.isBefore(weekStart) && !recordDate.isAfter(weekEnd);
                          })
                      .map(r -> DailyActivityReport.evaluate(null, r))
                      .toList();

              return new WeeklyActivityReportGroup(weekIndex + 1, weekStart, weekEnd, dailyReports);
            })
        .filter(Objects::nonNull) // 유효하지 않은 주차 제거
        .toList();
  }

  /** 위험 음식 섭취 일수 계산 FoodEvaluation에서 dangerous=true로 판정된 날의 수를 반환 */
  private static int countDangerousFoodDays(List<ActivityRecord> records) {
    if (records == null || records.isEmpty()) {
      return 0;
    }

    return (int)
        records.stream()
            .map(r -> FoodEvaluation.calculate(r.getFoodRecords(), DayType.TODAY))
            .filter(FoodEvaluation::isDangerous)
            .count();
  }

  public static MonthlyActivityReport empty(int lastMonthDangerousDays) {
    return new MonthlyActivityReport(lastMonthDangerousDays, 0, List.of());
  }

  public int size() {
    return weeklyActivityReportGroups.stream().mapToInt(week -> week.dailyReports().size()).sum();
  }

  private List<DailyActivityReport> getAllReports() {
    return weeklyActivityReportGroups.stream()
        .flatMap(week -> week.dailyReports().stream())
        .toList();
  }

  public List<FoodEvaluation> getAllFoodEvaluations() {
    return getAllReports().stream()
        .flatMap(report -> report.getFoodEvaluations().stream())
        .toList();
  }

  public List<StressEvaluation> getAllStressEvaluations() {
    return getAllReports().stream().map(DailyActivityReport::getStressEvaluation).toList();
  }

  public List<WaterEvaluation> getAllWaterEvaluations() {
    return getAllReports().stream()
        .flatMap(report -> report.getWaterEvaluations().stream())
        .toList();
  }

  /** 주차별 평균 스트레스 점수 계산 결과 */
  public record WeeklyAverageStress(
      int weekIndex, double averageScore, StressEvaluation representative) {}

  /** 주차별 평균 스트레스 점수 계산 NONE을 제외한 스트레스 값들의 점수 평균 계산 (20, 40, 60, 80, 100) */
  public List<WeeklyAverageStress> getWeeklyAverageStress() {
    return weeklyActivityReportGroups.stream()
        .map(
            wg -> {
              List<StressEvaluation> weeklyStress =
                  wg.dailyReports().stream()
                      .map(DailyActivityReport::getStressEvaluation)
                      .filter(stress -> stress != null && stress != StressEvaluation.NONE)
                      .toList();

              if (weeklyStress.isEmpty()) {
                return new WeeklyAverageStress(wg.weekIndex(), 0.0, StressEvaluation.NONE);
              }

              // 점수 기반 평균 계산
              double averageScore =
                  weeklyStress.stream().mapToInt(StressEvaluation::getScore).average().orElse(0.0);

              // 평균 점수에 가장 가까운 StressEvaluation 선택
              StressEvaluation representative = StressEvaluation.fromScore(averageScore);

              return new WeeklyAverageStress(wg.weekIndex(), averageScore, representative);
            })
        .toList();
  }

  /** 주차별 평균 스트레스 중 점수가 가장 높은 스트레스 반환 (점수가 높을수록 좋은 상태) */
  public StressEvaluation getBestAverageStress() {
    return getWeeklyAverageStress().stream()
        .filter(w -> w.representative() != StressEvaluation.NONE)
        .max(Comparator.comparingDouble(WeeklyAverageStress::averageScore))
        .map(WeeklyAverageStress::representative)
        .orElse(StressEvaluation.NONE);
  }

  /** 전체 월의 평균 스트레스 계산 모든 일일 스트레스의 점수 평균을 계산하여 반환 */
  public StressEvaluation getMonthlyAverageStress() {
    List<StressEvaluation> allStress =
        weeklyActivityReportGroups.stream()
            .flatMap(wg -> wg.dailyReports().stream())
            .map(DailyActivityReport::getStressEvaluation)
            .filter(stress -> stress != null && stress != StressEvaluation.NONE)
            .toList();

    if (allStress.isEmpty()) {
      return StressEvaluation.NONE;
    }

    double averageScore =
        allStress.stream().mapToInt(StressEvaluation::getScore).average().orElse(0.0);

    return StressEvaluation.fromScore(averageScore);
  }
}
