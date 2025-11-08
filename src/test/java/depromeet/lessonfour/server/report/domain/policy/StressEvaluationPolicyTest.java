package depromeet.lessonfour.server.report.domain.policy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import depromeet.lessonfour.server.activityrecord.domain.vo.StressLevel;
import depromeet.lessonfour.server.report.domain.vo.StressEvaluation;

@DisplayName("StressEvaluationPolicy 테스트")
class StressEvaluationPolicyTest {

  StressEvaluationPolicy policy = new StressEvaluationPolicy();

  @Test
  @DisplayName("스트레스 레벨에 따른 스트레스 평가가 올바르게 계산된다.")
  void givenStressLevel_whenCalculate_thenReturnCorrectStressEvaluation() {
    assertThat(policy.calculate(StressLevel.VERY_LOW)).isEqualTo(StressEvaluation.VERY_LOW);
    assertThat(policy.calculate(StressLevel.LOW)).isEqualTo(StressEvaluation.LOW);
    assertThat(policy.calculate(StressLevel.MEDIUM)).isEqualTo(StressEvaluation.MEDIUM);
    assertThat(policy.calculate(StressLevel.HIGH)).isEqualTo(StressEvaluation.HIGH);
    assertThat(policy.calculate(StressLevel.VERY_HIGH)).isEqualTo(StressEvaluation.VERY_HIGH);
    assertThat(policy.calculate(null)).isEqualTo(StressEvaluation.NONE);
  }
}
