package depromeet.lessonfour.server.report.domain.vo;

import java.util.List;

import lombok.Getter;

@Getter
public class ActivityReport {

  private final List<FoodEvaluation> foodEvaluations;
  private final WaterEvaluation waterEvaluation;
  private final StressEvaluation stressEvaluation;

  public ActivityReport(
      List<FoodEvaluation> foodEvaluations,
      WaterEvaluation waterEvaluation,
      StressEvaluation stressEvaluation) {
    this.foodEvaluations = foodEvaluations;
    this.waterEvaluation = waterEvaluation;
    this.stressEvaluation = stressEvaluation;
  }

  public boolean hasStress() {
    return stressEvaluation == StressEvaluation.HIGH
        || stressEvaluation == StressEvaluation.VERY_HIGH;
  }

  public boolean isStressWellManaged() {
    return stressEvaluation == StressEvaluation.LOW
        || stressEvaluation == StressEvaluation.VERY_LOW;
  }

  public boolean hasDangerousFood() {
    return foodEvaluations.stream().anyMatch(FoodEvaluation::isDangerous);
  }
}
