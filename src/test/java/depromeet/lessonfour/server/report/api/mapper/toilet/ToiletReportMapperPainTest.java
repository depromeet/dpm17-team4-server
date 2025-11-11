package depromeet.lessonfour.server.report.api.mapper.toilet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyPainSection;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyPainSection.ToiletPainComparison;
import depromeet.lessonfour.server.report.domain.vo.MonthlyReport;
import depromeet.lessonfour.server.report.domain.vo.toilet.ToiletPainDistribution;

class ToiletReportMapperPainTest {

  private ToiletReportMapper mapper;
  private MonthlyReport mockReport;

  @BeforeEach
  void setUp() {
    mapper = new ToiletReportMapper();
    mockReport = mock(MonthlyReport.class);
  }

  @Test
  @DisplayName("통증 분포를 올바르게 매핑한다")
  void givenPainDistribution_whenMapPain_thenReturnCorrectSection() {
    // given
    ToiletPainDistribution distribution = new ToiletPainDistribution(5, 8, 6, 4, 2, 3);
    when(mockReport.getToiletPainDistribution()).thenReturn(distribution);

    // when
    MonthlyPainSection result = mapper.mapPain(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("이번 달은 배를 부여잡은 날들이\n6회 있었어요");
    assertThat(result.veryLow()).isEqualTo(5);
    assertThat(result.low()).isEqualTo(8);
    assertThat(result.medium()).isEqualTo(6);
    assertThat(result.high()).isEqualTo(4);
    assertThat(result.veryHigh()).isEqualTo(2);
    assertThat(result.comparison()).isNotNull();
  }

  @Test
  @DisplayName("통증이 증가했을 때 increased 상태를 반환한다")
  void givenIncreasedPain_whenFromPainDiff_thenReturnIncreasedComparison() {
    // given
    int painDiff = 5;

    // when
    ToiletPainComparison result = mapper.fromPainDiff(painDiff);

    // then
    assertThat(result).isNotNull();
    assertThat(result.direction()).isEqualTo("increased");
    assertThat(result.count()).isEqualTo(5);
  }

  @Test
  @DisplayName("통증이 감소했을 때 decreased 상태를 반환한다")
  void givenDecreasedPain_whenFromPainDiff_thenReturnDecreasedComparison() {
    // given
    int painDiff = -3;

    // when
    ToiletPainComparison result = mapper.fromPainDiff(painDiff);

    // then
    assertThat(result).isNotNull();
    assertThat(result.direction()).isEqualTo("decreased");
    assertThat(result.count()).isEqualTo(3);
  }

  @Test
  @DisplayName("통증이 동일할 때 same 상태를 반환한다")
  void givenSamePain_whenFromPainDiff_thenReturnSameComparison() {
    // given
    int painDiff = 0;

    // when
    ToiletPainComparison result = mapper.fromPainDiff(painDiff);

    // then
    assertThat(result).isNotNull();
    assertThat(result.direction()).isEqualTo("same");
    assertThat(result.count()).isEqualTo(0);
  }

  @Test
  @DisplayName("통증이 큰 폭으로 증가했을 때 올바르게 처리한다")
  void givenLargeIncrease_whenFromPainDiff_thenReturnCorrectComparison() {
    // given
    int painDiff = 15;

    // when
    ToiletPainComparison result = mapper.fromPainDiff(painDiff);

    // then
    assertThat(result).isNotNull();
    assertThat(result.direction()).isEqualTo("increased");
    assertThat(result.count()).isEqualTo(15);
  }

  @Test
  @DisplayName("통증이 큰 폭으로 감소했을 때 올바르게 처리한다")
  void givenLargeDecrease_whenFromPainDiff_thenReturnCorrectComparison() {
    // given
    int painDiff = -12;

    // when
    ToiletPainComparison result = mapper.fromPainDiff(painDiff);

    // then
    assertThat(result).isNotNull();
    assertThat(result.direction()).isEqualTo("decreased");
    assertThat(result.count()).isEqualTo(12);
  }

  @Test
  @DisplayName("통증이 없는 경우를 올바르게 처리한다")
  void givenNoPain_whenMapPain_thenReturnCorrectSection() {
    // given
    ToiletPainDistribution distribution = new ToiletPainDistribution(25, 0, 0, 0, 0, 0);
    when(mockReport.getToiletPainDistribution()).thenReturn(distribution);

    // when
    MonthlyPainSection result = mapper.mapPain(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("이번 달은 배를 부여잡은 날들이\n0회 있었어요");
    assertThat(result.veryLow()).isEqualTo(25);
    assertThat(result.low()).isEqualTo(0);
    assertThat(result.medium()).isEqualTo(0);
    assertThat(result.high()).isEqualTo(0);
    assertThat(result.veryHigh()).isEqualTo(0);
  }

  @Test
  @DisplayName("매우 높은 통증만 있는 경우를 올바르게 처리한다")
  void givenOnlyVeryHighPain_whenMapPain_thenReturnCorrectSection() {
    // given
    ToiletPainDistribution distribution = new ToiletPainDistribution(0, 0, 0, 0, 15, 15);
    when(mockReport.getToiletPainDistribution()).thenReturn(distribution);

    // when
    MonthlyPainSection result = mapper.mapPain(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("이번 달은 배를 부여잡은 날들이\n15회 있었어요");
    assertThat(result.veryLow()).isEqualTo(0);
    assertThat(result.low()).isEqualTo(0);
    assertThat(result.medium()).isEqualTo(0);
    assertThat(result.high()).isEqualTo(0);
    assertThat(result.veryHigh()).isEqualTo(15);
  }

  @Test
  @DisplayName("균등한 통증 분포를 올바르게 처리한다")
  void givenEvenDistribution_whenMapPain_thenReturnCorrectSection() {
    // given
    ToiletPainDistribution distribution = new ToiletPainDistribution(5, 5, 5, 5, 5, 10);
    when(mockReport.getToiletPainDistribution()).thenReturn(distribution);

    // when
    MonthlyPainSection result = mapper.mapPain(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("이번 달은 배를 부여잡은 날들이\n10회 있었어요");
    assertThat(result.veryLow()).isEqualTo(5);
    assertThat(result.low()).isEqualTo(5);
    assertThat(result.medium()).isEqualTo(5);
    assertThat(result.high()).isEqualTo(5);
    assertThat(result.veryHigh()).isEqualTo(5);
    assertThat(result.comparison()).isNotNull();
  }

  @Test
  @DisplayName("통증 차이가 1일 때 올바르게 처리한다")
  void givenPainDiffOfOne_whenFromPainDiff_thenReturnCorrectComparison() {
    // given
    int painDiff = 1;

    // when
    ToiletPainComparison result = mapper.fromPainDiff(painDiff);

    // then
    assertThat(result).isNotNull();
    assertThat(result.direction()).isEqualTo("increased");
    assertThat(result.count()).isEqualTo(1);
  }

  @Test
  @DisplayName("통증 차이가 -1일 때 올바르게 처리한다")
  void givenPainDiffOfMinusOne_whenFromPainDiff_thenReturnCorrectComparison() {
    // given
    int painDiff = -1;

    // when
    ToiletPainComparison result = mapper.fromPainDiff(painDiff);

    // then
    assertThat(result).isNotNull();
    assertThat(result.direction()).isEqualTo("decreased");
    assertThat(result.count()).isEqualTo(1);
  }
}
