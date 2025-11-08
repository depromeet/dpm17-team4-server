package depromeet.lessonfour.server.report.domain.vo;

import lombok.Getter;

@Getter
public class WaterEvaluation {

  private final Integer quantity;
  private final WaterLevel level;
  private final DayType dayType;

  public WaterEvaluation(Integer quantity, WaterLevel level, DayType dayType) {
    this.quantity = quantity;
    this.level = level;
    this.dayType = dayType;
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
}
