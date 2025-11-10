package depromeet.lessonfour.server.report.domain.vo.activity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WaterEvaluationTest {

  @Test
  @DisplayName("물 섭취량이 null인 경우 빈 평가 객체를 반환한다.")
  void givenNullQuantity_whenCalculate_thenReturnEmptyEvaluation() {
    // given
    Integer quantity = null;
    DayType dayType = DayType.TODAY;

    // when
    WaterEvaluation evaluation = WaterEvaluation.calculate(quantity, dayType);

    // then
    assertThat(evaluation.getQuantity()).isZero();
    assertThat(evaluation.getLevel()).isEqualTo(WaterLevel.NONE);
  }

  @Test
  @DisplayName("물 섭취량이 null인 경우 생성되는 평가 객체의 DayType이 올바르게 설정된다.")
  void givenNullQuantity_whenCalculate_thenReturnEvaluationWithCorrectDayType() {
    // given
    Integer quantity = null;
    DayType dayType = DayType.YESTERDAY;

    // when
    WaterEvaluation evaluation = WaterEvaluation.calculate(quantity, dayType);

    // then
    assertThat(evaluation.getDayType()).isEqualTo(DayType.YESTERDAY);
  }

  @Test
  @DisplayName("물 섭취량이 HIGH_THRESHOLD인 경우 HIGH 레벨의 평가 객체를 반환한다.")
  void givenHighQuantity_whenCalculate_thenReturnHighLevelEvaluation() {
    // given
    Integer quantity = WaterEvaluation.HIGH_THRESHOLD;
    DayType dayType = DayType.TODAY;

    // when
    WaterEvaluation evaluation = WaterEvaluation.calculate(quantity, dayType);

    // then
    assertThat(evaluation.getLevel()).isEqualTo(WaterLevel.HIGH);
  }

  @Test
  @DisplayName("물 섭취량이 MEDIUM_THRESHOLD인 경우 MEDIUM 레벨의 평가 객체를 반환한다.")
  void givenMediumQuantity_whenCalculate_thenReturnMediumLevelEvaluation() {
    // given
    Integer quantity = WaterEvaluation.MEDIUM_THRESHOLD;
    DayType dayType = DayType.TODAY;

    // when
    WaterEvaluation evaluation = WaterEvaluation.calculate(quantity, dayType);

    // then
    assertThat(evaluation.getLevel()).isEqualTo(WaterLevel.MEDIUM);
  }

  @Test
  @DisplayName("물 섭취량이 MEDIUM_THRESHOLD 미만인 경우 LOW 레벨의 평가 객체를 반환한다.")
  void givenLowQuantity_whenCalculate_thenReturnLowLevelEvaluation() {
    // given
    Integer quantity = WaterEvaluation.MEDIUM_THRESHOLD - 1;
    DayType dayType = DayType.TODAY;

    // when
    WaterEvaluation evaluation = WaterEvaluation.calculate(quantity, dayType);

    // then
    assertThat(evaluation.getLevel()).isEqualTo(WaterLevel.LOW);
  }
}
