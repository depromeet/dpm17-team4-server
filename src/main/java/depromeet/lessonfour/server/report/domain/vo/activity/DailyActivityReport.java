package depromeet.lessonfour.server.report.domain.vo.activity;

import java.util.ArrayList;
import java.util.List;

import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DailyActivityReport {

  private final List<FoodEvaluation> foodEvaluations;
  private final List<WaterEvaluation> waterEvaluations;
  private final StressEvaluation stressEvaluation;

  public static DailyActivityReport empty() {
    return new DailyActivityReport(List.of(), List.of(), StressEvaluation.NONE);
  }

  public static DailyActivityReport evaluate(ActivityRecord previous, ActivityRecord current) {
    List<FoodEvaluation> foodEvaluations = new ArrayList<>();
    List<WaterEvaluation> waterEvaluations = new ArrayList<>();
    StressEvaluation stressEvaluation = StressEvaluation.empty();

    if (previous != null) {
      foodEvaluations.add(FoodEvaluation.calculate(previous.getFoodRecords(), DayType.YESTERDAY));
      waterEvaluations.add(
          WaterEvaluation.calculate(previous.getWaterIntakeCups(), DayType.YESTERDAY));
    }

    if (current != null) {
      foodEvaluations.add(FoodEvaluation.calculate(current.getFoodRecords(), DayType.TODAY));
      waterEvaluations.add(WaterEvaluation.calculate(current.getWaterIntakeCups(), DayType.TODAY));
      stressEvaluation = StressEvaluation.calculate(current.getStressLevel());
    }

    return new DailyActivityReport(foodEvaluations, waterEvaluations, stressEvaluation);
  }

  public boolean hasDangerousFood() {
    return foodEvaluations.stream().anyMatch(FoodEvaluation::isDangerous);
  }
}
