package depromeet.lessonfour.server.report.domain.vo;

import lombok.Getter;

@Getter
public class WaterEvaluation {

  private final int quantity;
  private final WaterLevel level;
  private final DayType dayType;

  public WaterEvaluation(int quantity, WaterLevel level, DayType dayType) {
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
}
