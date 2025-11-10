package depromeet.lessonfour.server.report.domain.vo;

import java.util.List;

import depromeet.lessonfour.server.report.domain.vo.daily.DailyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.monthly.MonthlyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.monthly.MonthlyToiletReport;
import depromeet.lessonfour.server.report.domain.vo.weekly.WeeklyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.weekly.WeeklyToiletReport;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class SuggestionContext {

  private final List<FoodEvaluation> foodEvaluations;
  private final List<WaterEvaluation> waterEvaluations;
  private final List<StressEvaluation> stressEvaluations;
  private final List<ToiletEvaluation> toiletEvaluations;

  public static SuggestionContext weekly(
      WeeklyActivityReport activityReport, WeeklyToiletReport toiletReport) {

    List<FoodEvaluation> foodEvaluations =
        activityReport.getDailyReports().stream()
            .flatMap(daily -> daily.getFoodEvaluations().stream())
            .toList();

    List<WaterEvaluation> waterEvaluations =
        activityReport.getDailyReports().stream()
            .flatMap(daily -> daily.getWaterEvaluations().stream())
            .toList();

    List<StressEvaluation> stressEvaluations =
        activityReport.getDailyReports().stream()
            .map(DailyActivityReport::getStressEvaluation)
            .toList();

    List<ToiletEvaluation> toiletEvaluations =
        toiletReport.getDailyReports().stream()
            .flatMap(daily -> daily.getItems().stream())
            .toList();

    return SuggestionContext.builder()
        .foodEvaluations(foodEvaluations)
        .waterEvaluations(waterEvaluations)
        .stressEvaluations(stressEvaluations)
        .toiletEvaluations(toiletEvaluations)
        .build();
  }

  public static SuggestionContext monthly(
      MonthlyActivityReport activityReport, MonthlyToiletReport toiletReport) {
    List<DailyActivityReport> allReports = activityReport.getAllReports();

    List<FoodEvaluation> foodEvaluations =
        allReports.stream().flatMap(daily -> daily.getFoodEvaluations().stream()).toList();

    List<WaterEvaluation> waterEvaluations =
        allReports.stream().flatMap(daily -> daily.getWaterEvaluations().stream()).toList();

    List<StressEvaluation> stressEvaluations =
        allReports.stream().map(DailyActivityReport::getStressEvaluation).toList();

    List<ToiletEvaluation> toiletEvaluations =
        toiletReport.dailyReports().stream().flatMap(daily -> daily.getItems().stream()).toList();

    return SuggestionContext.builder()
        .foodEvaluations(foodEvaluations)
        .waterEvaluations(waterEvaluations)
        .stressEvaluations(stressEvaluations)
        .toiletEvaluations(toiletEvaluations)
        .build();
  }
}
