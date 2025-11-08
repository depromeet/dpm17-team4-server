package depromeet.lessonfour.server.report.domain.policy;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.activityrecord.domain.vo.StressLevel;
import depromeet.lessonfour.server.report.domain.vo.StressEvaluation;

@Component
public class StressEvaluationPolicy {

  public StressEvaluation calculate(StressLevel stressLevel) {
    return switch (stressLevel) {
      case VERY_LOW -> StressEvaluation.VERY_LOW;
      case LOW -> StressEvaluation.LOW;
      case MEDIUM -> StressEvaluation.MEDIUM;
      case HIGH -> StressEvaluation.HIGH;
      case VERY_HIGH -> StressEvaluation.VERY_HIGH;
      case null -> StressEvaluation.NONE;
    };
  }
}
