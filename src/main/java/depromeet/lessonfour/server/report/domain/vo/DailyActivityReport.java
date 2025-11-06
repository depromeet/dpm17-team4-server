package depromeet.lessonfour.server.report.domain.vo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DailyActivityReport {

  private final List<FoodEvaluation> foodEvaluations;
  private final List<WaterEvaluation> waterEvaluations;
  private final StressEvaluation stressEvaluation;

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
