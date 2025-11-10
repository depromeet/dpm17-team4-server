package depromeet.lessonfour.server.report.domain.vo.activity;

import java.util.Arrays;

import depromeet.lessonfour.server.activityrecord.domain.vo.StressLevel;
import lombok.Getter;

@Getter
public enum StressEvaluation {
  VERY_LOW(20),
  LOW(40),
  MEDIUM(60),
  HIGH(80),
  VERY_HIGH(100),
  NONE(0);

  private final int score;

  StressEvaluation(int score) {
    this.score = score;
  }

  public static StressEvaluation calculate(StressLevel stressLevel) {
    return switch (stressLevel) {
      case VERY_LOW -> VERY_LOW;
      case LOW -> LOW;
      case MEDIUM -> MEDIUM;
      case HIGH -> HIGH;
      case VERY_HIGH -> VERY_HIGH;
      case null -> NONE;
    };
  }

  /** 점수를 스트레스 레벨로 변환 가장 가까운 레벨을 반환 */
  public static StressEvaluation fromScore(double score) {
    if (score <= 0) return NONE;

    return Arrays.stream(values())
        .filter(s -> s != NONE)
        .min((s1, s2) -> Double.compare(Math.abs(s1.score - score), Math.abs(s2.score - score)))
        .orElse(NONE);
  }

  public static StressEvaluation empty() {
    return NONE;
  }

  public boolean isHigh() {
    return this == HIGH || this == VERY_HIGH;
  }
}
