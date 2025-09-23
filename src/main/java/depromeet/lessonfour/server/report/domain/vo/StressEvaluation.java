package depromeet.lessonfour.server.report.domain.vo;

public enum StressEvaluation {
  VERY_LOW,
  LOW,
  MEDIUM,
  HIGH,
  VERY_HIGH,
  NONE;

  public static StressEvaluation empty() {
    return NONE;
  }
}
