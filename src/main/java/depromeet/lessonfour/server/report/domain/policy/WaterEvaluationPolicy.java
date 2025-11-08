package depromeet.lessonfour.server.report.domain.policy;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.report.domain.vo.DayType;
import depromeet.lessonfour.server.report.domain.vo.WaterEvaluation;
import depromeet.lessonfour.server.report.domain.vo.WaterLevel;

@Component
public class WaterEvaluationPolicy {

  static final int HIGH_THRESHOLD = 8;
  static final int MEDIUM_THRESHOLD = 5;

  public WaterEvaluation calculate(Integer quantity, DayType dayType) {
    if (quantity == null) {
      return WaterEvaluation.empty(dayType);
    }

    if (quantity >= HIGH_THRESHOLD) {
      return new WaterEvaluation(quantity, WaterLevel.HIGH, dayType);
    }

    if (quantity >= MEDIUM_THRESHOLD) {
      return new WaterEvaluation(quantity, WaterLevel.MEDIUM, dayType);
    }

    return new WaterEvaluation(quantity, WaterLevel.LOW, dayType);
  }
}
