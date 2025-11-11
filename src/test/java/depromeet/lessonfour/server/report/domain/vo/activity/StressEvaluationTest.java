package depromeet.lessonfour.server.report.domain.vo.activity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import depromeet.lessonfour.server.activityrecord.domain.vo.StressLevel;

class StressEvaluationTest {

  @Test
  @DisplayName("스트레스 레벨에 따른 스트레스 평가가 올바르게 계산된다.")
  void givenStressLevel_whenCalculate_thenReturnCorrectStressEvaluation() {
    assertThat(StressEvaluation.calculate(StressLevel.VERY_LOW))
        .isEqualTo(StressEvaluation.VERY_LOW);
    assertThat(StressEvaluation.calculate(StressLevel.LOW)).isEqualTo(StressEvaluation.LOW);
    assertThat(StressEvaluation.calculate(StressLevel.MEDIUM)).isEqualTo(StressEvaluation.MEDIUM);
    assertThat(StressEvaluation.calculate(StressLevel.HIGH)).isEqualTo(StressEvaluation.HIGH);
    assertThat(StressEvaluation.calculate(StressLevel.VERY_HIGH))
        .isEqualTo(StressEvaluation.VERY_HIGH);
    assertThat(StressEvaluation.calculate(null)).isEqualTo(StressEvaluation.NONE);
  }
}
