package depromeet.lessonfour.server.report.domain.vo.activity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WaterEvaluation {

  static final int HIGH_THRESHOLD = 8;
  static final int MEDIUM_THRESHOLD = 5;

  private final Integer quantity;
  private final WaterLevel level;
  private final DayType dayType;

  public static WaterEvaluation calculate(Integer quantity, DayType dayType) {
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

  public static WaterEvaluation empty() {
    return new WaterEvaluation(0, WaterLevel.NONE, DayType.NONE);
  }

  public static WaterEvaluation empty(DayType dayType) {
    return new WaterEvaluation(0, WaterLevel.NONE, dayType);
  }

  public int getQuantity() {
    return quantity == null ? 0 : quantity;
  }

  public boolean isNormal() {
    return level == WaterLevel.MEDIUM;
  }

  public boolean isLack() {
    return level == WaterLevel.LOW;
  }
}
