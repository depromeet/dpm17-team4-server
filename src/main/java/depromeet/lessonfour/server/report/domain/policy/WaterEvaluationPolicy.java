package depromeet.lessonfour.server.report.domain.policy;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.report.domain.vo.WaterEvaluation;
import depromeet.lessonfour.server.report.domain.vo.WaterLevel;

@Component
public class WaterEvaluationPolicy {

  private static final int HIGH_THRESHOLD = 8;
  private static final int MEDIUM_THRESHOLD = 5;

  public WaterEvaluation calculate(int quantity) {
    if (quantity >= HIGH_THRESHOLD) {
      return new WaterEvaluation(quantity, WaterLevel.HIGH);
    } else if (quantity >= MEDIUM_THRESHOLD) {
      return new WaterEvaluation(quantity, WaterLevel.MEDIUM);
    } else {
      return new WaterEvaluation(quantity, WaterLevel.LOW);
    }
  }
}
