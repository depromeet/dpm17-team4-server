package depromeet.lessonfour.server.report.api.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import depromeet.lessonfour.server.report.api.dto.response.GetDailyReportResponseDto.StressReport;
import depromeet.lessonfour.server.report.domain.vo.StressEvaluation;

@DisplayName("StressMapper 테스트")
class StressMapperTest {

  StressMapper mapper = new StressMapper();

  @Test
  @DisplayName("VERY_LOW 스트레스 평가는 적절한 메시지와 이미지를 반환한다")
  void givenVeryLowStressEvaluation_whenMap_thenReturnCorrectStressReport() {
    // given
    StressEvaluation evaluation = StressEvaluation.VERY_LOW;

    // when
    StressReport result = mapper.map(evaluation);

    // then
    assertThat(result).isNotNull();
    assertThat(result.message()).isEqualTo("삐용삐용! 스트레스 만땅! 산책이나 명상을 해볼까요?");
    assertThat(result.image())
        .isEqualTo(
            "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/condition_very_bad.png");
  }

  @Test
  @DisplayName("LOW 스트레스 평가는 적절한 메시지와 이미지를 반환한다")
  void givenLowStressEvaluation_whenMap_thenReturnCorrectStressReport() {
    // given
    StressEvaluation evaluation = StressEvaluation.LOW;

    // when
    StressReport result = mapper.map(evaluation);

    // then
    assertThat(result).isNotNull();
    assertThat(result.message()).isEqualTo("스트레스에 잡아먹히지 않도록 조심해봐요!");
    assertThat(result.image())
        .isEqualTo(
            "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/condition_bad.png");
  }

  @Test
  @DisplayName("MEDIUM 스트레스 평가는 적절한 메시지와 이미지를 반환한다")
  void givenMediumStressEvaluation_whenMap_thenReturnCorrectStressReport() {
    // given
    StressEvaluation evaluation = StressEvaluation.MEDIUM;

    // when
    StressReport result = mapper.map(evaluation);

    // then
    assertThat(result).isNotNull();
    assertThat(result.message()).isEqualTo("스트레스 발생! 마음을 다스릴 수 있도록 노력해요");
    assertThat(result.image())
        .isEqualTo(
            "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/condition_medium.png");
  }

  @Test
  @DisplayName("HIGH 스트레스 평가는 적절한 메시지와 이미지를 반환한다")
  void givenHighStressEvaluation_whenMap_thenReturnCorrectStressReport() {
    // given
    StressEvaluation evaluation = StressEvaluation.HIGH;

    // when
    StressReport result = mapper.map(evaluation);

    // then
    assertThat(result).isNotNull();
    assertThat(result.message()).isEqualTo("긍정적인 당신! 그 마인드 오래도록 유지해봐요");
    assertThat(result.image())
        .isEqualTo(
            "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/condition_good.png");
  }

  @Test
  @DisplayName("VERY_HIGH 스트레스 평가는 적절한 메시지와 이미지를 반환한다")
  void givenVeryHighStressEvaluation_whenMap_thenReturnCorrectStressReport() {
    // given
    StressEvaluation evaluation = StressEvaluation.VERY_HIGH;

    // when
    StressReport result = mapper.map(evaluation);

    // then
    assertThat(result).isNotNull();
    assertThat(result.message()).isEqualTo("스트레스 Zero! 행복한 하루가 되셨군요?");
    assertThat(result.image())
        .isEqualTo(
            "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/condition_very_good.png");
  }

  @Test
  @DisplayName("NONE 스트레스 평가는 null을 반환한다")
  void givenNoneStressEvaluation_whenMap_thenReturnNull() {
    // given
    StressEvaluation evaluation = StressEvaluation.NONE;

    // when
    StressReport result = mapper.map(evaluation);

    // then
    assertThat(result).isNull();
  }
}
