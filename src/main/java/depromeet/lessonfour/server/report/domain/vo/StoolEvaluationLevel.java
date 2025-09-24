package depromeet.lessonfour.server.report.domain.vo;

public enum StoolEvaluationLevel {
  VERY_GOOD,
  GOOD,
  AVERAGE,
  BAD,
  VERY_BAD,
  NONE;

  private static final int VERY_GOOD_THRESHOLD = 80;
  private static final int GOOD_THRESHOLD = 60;
  private static final int AVERAGE_THRESHOLD = 40;
  private static final int BAD_THRESHOLD = 20;

  public static StoolEvaluationLevel from(int score) {
    if (score >= VERY_GOOD_THRESHOLD) {
      return VERY_GOOD;
    } else if (score >= GOOD_THRESHOLD) {
      return GOOD;
    } else if (score >= AVERAGE_THRESHOLD) {
      return AVERAGE;
    } else if (score >= BAD_THRESHOLD) {
      return BAD;
    } else {
      return VERY_BAD;
    }
  }

  public static StoolEvaluationLevel from(double score) {
    return from((int) score);
  }
}
