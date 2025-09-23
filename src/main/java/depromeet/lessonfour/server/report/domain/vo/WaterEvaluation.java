package depromeet.lessonfour.server.report.domain.vo;

public class WaterEvaluation {
  private final int quantity;
  private final WaterLevel level;

  public WaterEvaluation(int quantity, WaterLevel level) {
    this.quantity = quantity;
    this.level = level;
  }
}
