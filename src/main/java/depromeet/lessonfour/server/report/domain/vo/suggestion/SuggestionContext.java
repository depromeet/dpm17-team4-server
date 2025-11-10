package depromeet.lessonfour.server.report.domain.vo.suggestion;

import java.util.List;

import depromeet.lessonfour.server.report.domain.vo.activity.DailyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.activity.FoodEvaluation;
import depromeet.lessonfour.server.report.domain.vo.activity.MonthlyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.activity.StressEvaluation;
import depromeet.lessonfour.server.report.domain.vo.activity.WaterEvaluation;
import depromeet.lessonfour.server.report.domain.vo.activity.WeeklyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.toilet.MonthlyToiletReport;
import depromeet.lessonfour.server.report.domain.vo.toilet.ToiletEvaluation;
import depromeet.lessonfour.server.report.domain.vo.toilet.WeeklyToiletReport;
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
    List<FoodEvaluation> foodEvaluations = activityReport.getAllFoodEvaluations();

    List<WaterEvaluation> waterEvaluations = activityReport.getAllWaterEvaluations();

    List<StressEvaluation> stressEvaluations = activityReport.getAllStressEvaluations();

    List<ToiletEvaluation> toiletEvaluations = toiletReport.getAllEvaluations();

    return SuggestionContext.builder()
        .foodEvaluations(foodEvaluations)
        .waterEvaluations(waterEvaluations)
        .stressEvaluations(stressEvaluations)
        .toiletEvaluations(toiletEvaluations)
        .build();
  }
}
