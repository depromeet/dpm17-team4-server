package depromeet.lessonfour.server.report.domain.vo.activity;

import java.time.LocalDate;
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

  /** 이번 주에 기록된 음식 평가가 하나라도 있는지 여부 */
  public boolean hasFoodRecords() {
    return dailyReports.stream()
        .flatMap(report -> report.getFoodEvaluations().stream())
        .findAny()
        .isPresent();
  }

  public boolean hasDangerousFood() {
    return dailyReports.stream().anyMatch(DailyActivityReport::hasDangerousFood);
  }

  public int getLastWeekDangerousFoodDays() {
    return (int) dailyReports.stream().filter(DailyActivityReport::hasDangerousFood).count();
  }

  public int getThisWeekDangerousFoodDays() {
    return (int) dailyReports.stream().filter(DailyActivityReport::hasDangerousFood).count();
  }

  public List<FoodsByMealTime> aggregateFoodsByMealTime(LocalDate weekStartDate) {
    return dailyReports.stream()
        // DailyActivityReport → FoodEvaluation → FoodsByMealTime
        .flatMap(
            report ->
                report.getFoodEvaluations().stream()
                    .flatMap(
                        foodEval ->
                            foodEval.getFoodsByMealTime().entrySet().stream()
                                // 빈 리스트는 제외
                                .filter(
                                    entry ->
                                        entry.getValue() != null && !entry.getValue().isEmpty())
                                // FoodsByMealTime으로 매핑
                                .map(
                                    entry ->
                                        new FoodsByMealTime(
                                            report.getOccurredAt().toDate(), // 발생 날짜
                                            entry.getKey(), // MealTime
                                            entry.getValue() // 음식 리스트
                                            ))))
        // 날짜 → mealTime 순 정렬
        .sorted(
            Comparator.comparing(FoodsByMealTime::occurredAt)
                .thenComparing(f -> f.mealTime().ordinal()))
        .toList();
  }
}
