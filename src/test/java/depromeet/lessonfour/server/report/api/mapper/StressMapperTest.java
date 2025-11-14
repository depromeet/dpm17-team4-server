package depromeet.lessonfour.server.report.api.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import depromeet.lessonfour.server.report.api.dto.response.GetDailyReportResponseDto.DailyStressReport;
import depromeet.lessonfour.server.report.api.mapper.activity.StressMapper;
import depromeet.lessonfour.server.report.domain.vo.activity.StressEvaluation;

class StressMapperTest {

  StressMapper mapper = new StressMapper();

  @Test
  @DisplayName("VERY_LOW 스트레스 평가는 적절한 메시지와 이미지를 반환한다")
  void givenVeryLowStressEvaluation_whenMap_Daily_thenReturnCorrectStressReport() {
    // given
    StressEvaluation evaluation = StressEvaluation.VERY_LOW;

    // when
    DailyStressReport result = mapper.mapDaily(evaluation);

    // then
    assertThat(result).isNotNull();
    assertThat(result.message()).isEqualTo("스트레스 Zero! 행복한 하루가 되셨군요?");
    assertThat(result.image())
        .isEqualTo(
            "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/stress/very_good.png");
  }

  @Test
  @DisplayName("LOW 스트레스 평가는 적절한 메시지와 이미지를 반환한다")
  void givenLowStressEvaluation_whenMap_Daily_thenReturnCorrectStressReport() {
    // given
    StressEvaluation evaluation = StressEvaluation.LOW;

    // when
    DailyStressReport result = mapper.mapDaily(evaluation);

    // then
    assertThat(result).isNotNull();
    assertThat(result.message()).isEqualTo("긍정적인 당신! 그 마인드 오래도록 유지해봐요");
    assertThat(result.image())
        .isEqualTo(
            "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/stress/good.png");
  }

  @Test
  @DisplayName("MEDIUM 스트레스 평가는 적절한 메시지와 이미지를 반환한다")
  void givenMediumStressEvaluation_whenMap_Daily_thenReturnCorrectStressReport() {
    // given
    StressEvaluation evaluation = StressEvaluation.MEDIUM;

    // when
    DailyStressReport result = mapper.mapDaily(evaluation);

    // then
    assertThat(result).isNotNull();
    assertThat(result.message()).isEqualTo("스트레스 발생! 마음을 다스릴 수 있도록 노력해요");
    assertThat(result.image())
        .isEqualTo(
            "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/stress/normal.png");
  }

  @Test
  @DisplayName("HIGH 스트레스 평가는 적절한 메시지와 이미지를 반환한다")
  void givenHighStressEvaluation_whenMap_Daily_thenReturnCorrectStressReport() {
    // given
    StressEvaluation evaluation = StressEvaluation.HIGH;

    // when
    DailyStressReport result = mapper.mapDaily(evaluation);

    // then
    assertThat(result).isNotNull();
    assertThat(result.message()).isEqualTo("스트레스에 잡아먹히지 않도록 조심해봐요!");
    assertThat(result.image())
        .isEqualTo(
            "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/stress/bad.png");
  }

  @Test
  @DisplayName("VERY_HIGH 스트레스 평가는 적절한 메시지와 이미지를 반환한다")
  void givenVeryHighStressEvaluation_whenMap_Daily_thenReturnCorrectStressReport() {
    // given
    StressEvaluation evaluation = StressEvaluation.VERY_HIGH;

    // when
    DailyStressReport result = mapper.mapDaily(evaluation);

    // then
    assertThat(result).isNotNull();
    assertThat(result.message()).isEqualTo("삐용삐용! 스트레스 만땅! 산책이나 명상을 해볼까요?");
    assertThat(result.image())
        .isEqualTo(
            "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/stress/very_bad.png");
  }

  @Test
  @DisplayName("NONE 스트레스 평가는 null을 반환한다")
  void givenNoneStressEvaluation_whenMap_Daily_thenReturnNull() {
    // given
    StressEvaluation evaluation = StressEvaluation.NONE;

    // when
    DailyStressReport result = mapper.mapDaily(evaluation);

    // then
    assertThat(result).isNull();
  }
}
