package depromeet.lessonfour.server.report.domain.policy;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.report.domain.vo.DayType;
import depromeet.lessonfour.server.report.domain.vo.WaterEvaluation;
import depromeet.lessonfour.server.report.domain.vo.WaterLevel;

@Component
public class WaterEvaluationPolicy {

  private static final int HIGH_THRESHOLD = 8;
  private static final int MEDIUM_THRESHOLD = 5;

  public WaterEvaluation calculate(int quantity, DayType dayType) {
    if (quantity >= HIGH_THRESHOLD) {
      return new WaterEvaluation(quantity, WaterLevel.HIGH, dayType);
    } else if (quantity >= MEDIUM_THRESHOLD) {
      return new WaterEvaluation(quantity, WaterLevel.MEDIUM, dayType);
    } else {
      return new WaterEvaluation(quantity, WaterLevel.LOW, dayType);
    }
  }
}
