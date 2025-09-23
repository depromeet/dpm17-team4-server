package depromeet.lessonfour.server.report.domain.vo;

import lombok.Getter;

@Getter
public class WaterEvaluation {
  private final int quantity;
  private final WaterLevel level;

  public WaterEvaluation(int quantity, WaterLevel level) {
    this.quantity = quantity;
    this.level = level;
  }

  public static WaterEvaluation empty() {
    return new WaterEvaluation(0, WaterLevel.NONE);
  }
}
