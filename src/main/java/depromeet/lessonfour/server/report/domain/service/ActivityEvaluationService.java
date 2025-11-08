package depromeet.lessonfour.server.report.domain.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import depromeet.lessonfour.server.report.domain.policy.FoodEvaluationPolicy;
import depromeet.lessonfour.server.report.domain.policy.StressEvaluationPolicy;
import depromeet.lessonfour.server.report.domain.policy.WaterEvaluationPolicy;
import depromeet.lessonfour.server.report.domain.vo.DailyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.DayType;
import depromeet.lessonfour.server.report.domain.vo.FoodEvaluation;
import depromeet.lessonfour.server.report.domain.vo.StressEvaluation;
import depromeet.lessonfour.server.report.domain.vo.WaterEvaluation;
import depromeet.lessonfour.server.report.domain.vo.monthly.MonthlyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.weekly.WeeklyActivityReport;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivityEvaluationService {

  private final FoodEvaluationPolicy foodEvaluationPolicy;
  private final WaterEvaluationPolicy waterEvaluationPolicy;
  private final StressEvaluationPolicy stressEvaluationPolicy;

  public DailyActivityReport evaluate(ActivityRecord previous, ActivityRecord current) {
    List<FoodEvaluation> foodEvaluations = new ArrayList<>();
    List<WaterEvaluation> waterEvaluations = new ArrayList<>();
    StressEvaluation stressEvaluation = StressEvaluation.empty();

    if (previous != null) {
      foodEvaluations.add(
          foodEvaluationPolicy.calculate(previous.getFoodRecords(), DayType.YESTERDAY));
      waterEvaluations.add(
          waterEvaluationPolicy.calculate(previous.getWaterIntakeCups(), DayType.YESTERDAY));
    }

    if (current != null) {
      foodEvaluations.add(foodEvaluationPolicy.calculate(current.getFoodRecords(), DayType.TODAY));
      waterEvaluations.add(
          waterEvaluationPolicy.calculate(current.getWaterIntakeCups(), DayType.TODAY));
      stressEvaluation = stressEvaluationPolicy.calculate(current.getStressLevel());
    }

    return new DailyActivityReport(foodEvaluations, waterEvaluations, stressEvaluation);
  }

  public WeeklyActivityReport evaluateWeekly(List<ActivityRecord> records) {
    if (records == null || records.isEmpty()) {
      return new WeeklyActivityReport(List.of());
    }

    // 날짜 오름차순 정렬 (뷰에서 월~일 순서대로 사용하기 좋게)
    List<ActivityRecord> sorted =
        records.stream()
            .sorted(
                Comparator.comparing(
                    r -> r.getActivityAt().toDate() // ActivityAt -> LocalDate
                    ))
            .toList();

    List<DailyActivityReport> dailyReports = new ArrayList<>();

    for (ActivityRecord record : sorted) {
      // 주간 컨텍스트에서는 전일 비교 필요 없으므로 previous = null
      DailyActivityReport dailyReport = evaluate(null, record);
      dailyReports.add(dailyReport);
    }

    return new WeeklyActivityReport(dailyReports);
  }

  public MonthlyActivityReport evaluateMonthly(
      List<ActivityRecord> currentMonth,
      List<ActivityRecord> lastMonth,
      LocalDate monthFirstDay,
      LocalDate monthLastDay) {
    if (currentMonth == null || currentMonth.isEmpty()) {
      int lastDangerous = 0;
      if (lastMonth != null && !lastMonth.isEmpty()) {
        lastDangerous =
            (int)
                lastMonth.stream()
                    .map(r -> foodEvaluationPolicy.calculate(r.getFoodRecords(), DayType.TODAY))
                    .filter(FoodEvaluation::isDangerous)
                    .count();
      }
      return MonthlyActivityReport.empty(lastDangerous);
    }

    // 날짜 오름차순
    List<ActivityRecord> sorted =
        currentMonth.stream()
            .sorted(Comparator.comparing(r -> r.getActivityAt().toDate()))
            .toList();

    // 1~7 / 8~14 / 15~21 / 22~28 / 29~end 버킷팅
    LocalDate start = monthFirstDay;
    LocalDate end = monthLastDay;

    List<MonthlyActivityReport.WeeklyGroup> groups = new ArrayList<>();
    int[] cuts = new int[] {7, 14, 21, 28, Integer.MAX_VALUE};

    for (int wi = 0; wi < 5; wi++) {
      final int upper = cuts[wi];
      LocalDate wStart = (wi == 0) ? start : start.withDayOfMonth(cuts[wi - 1] + 1);
      LocalDate wEnd = (upper == Integer.MAX_VALUE) ? end : start.withDayOfMonth(upper);
      List<ActivityRecord> inWeek =
          sorted.stream()
              .filter(
                  r -> {
                    LocalDate d = r.getActivityAt().toDate();
                    return !d.isBefore(wStart) && !d.isAfter(wEnd);
                  })
              .toList();
      List<DailyActivityReport> daily =
          inWeek.stream()
              .map(r -> evaluate(null, r)) // previous=null (주간과 동일 로직)
              .toList();
      groups.add(new MonthlyActivityReport.WeeklyGroup(wi + 1, wStart, wEnd, daily));
    }

    int dangerousDays =
        (int)
            sorted.stream()
                .map(r -> foodEvaluationPolicy.calculate(r.getFoodRecords(), DayType.TODAY))
                .filter(FoodEvaluation::isDangerous)
                .count();

    int lastDangerous = 0;
    if (lastMonth != null && !lastMonth.isEmpty()) {
      lastDangerous =
          (int)
              lastMonth.stream()
                  .map(r -> foodEvaluationPolicy.calculate(r.getFoodRecords(), DayType.TODAY))
                  .filter(FoodEvaluation::isDangerous)
                  .count();
    }

    return new MonthlyActivityReport(sorted.size(), groups, dangerousDays, lastDangerous);
  }
}
