package depromeet.lessonfour.server.report.domain.vo.toilet;

public record ToiletPainDistribution(
    int veryLow, int low, int medium, int high, int veryHigh, int painDiff) {

  public int getPainfulDay() {
    return high + veryHigh;
  }
}
