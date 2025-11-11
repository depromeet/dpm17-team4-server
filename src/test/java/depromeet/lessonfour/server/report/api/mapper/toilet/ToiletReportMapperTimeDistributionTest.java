package depromeet.lessonfour.server.report.api.mapper.toilet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyTimeDistributionSection;
import depromeet.lessonfour.server.report.domain.vo.MonthlyReport;
import depromeet.lessonfour.server.report.domain.vo.toilet.ToiletTimeDistribution;

class ToiletReportMapperTimeDistributionTest {

  private ToiletReportMapper mapper;
  private MonthlyReport mockReport;

  @BeforeEach
  void setUp() {
    mapper = new ToiletReportMapper();
    mockReport = mock(MonthlyReport.class);
  }

  @Test
  @DisplayName("5분 이내가 가장 많을 때 올바른 메시지를 반환한다")
  void givenWithin5MinMostFrequent_whenMapTimeDistribution_thenReturnCorrectMessage() {
    // given
    ToiletTimeDistribution distribution = new ToiletTimeDistribution(15, 8, 5);
    when(mockReport.getToiletTimeDistribution()).thenReturn(distribution);

    // when
    MonthlyTimeDistributionSection result = mapper.mapTimeDistribution(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("배변 소요 시간은\n주로 5분 이내였어요");
    assertThat(result.within5min()).isEqualTo(15);
    assertThat(result.over5min()).isEqualTo(8);
    assertThat(result.over10min()).isEqualTo(5);
    assertThat(result.warning()).isNull();
  }

  @Test
  @DisplayName("5분 이상이 가장 많을 때 올바른 메시지를 반환한다")
  void givenOver5MinMostFrequent_whenMapTimeDistribution_thenReturnCorrectMessage() {
    // given
    ToiletTimeDistribution distribution = new ToiletTimeDistribution(8, 18, 6);
    when(mockReport.getToiletTimeDistribution()).thenReturn(distribution);

    // when
    MonthlyTimeDistributionSection result = mapper.mapTimeDistribution(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("배변 소요 시간은\n주로 5분 이상이었어요");
    assertThat(result.within5min()).isEqualTo(8);
    assertThat(result.over5min()).isEqualTo(18);
    assertThat(result.over10min()).isEqualTo(6);
    assertThat(result.warning()).isNull();
  }

  @Test
  @DisplayName("10분 이상이 가장 많을 때 경고 메시지를 포함한다")
  void givenOver10MinMostFrequent_whenMapTimeDistribution_thenReturnWithWarning() {
    // given
    ToiletTimeDistribution distribution = new ToiletTimeDistribution(7, 9, 20);
    when(mockReport.getToiletTimeDistribution()).thenReturn(distribution);

    // when
    MonthlyTimeDistributionSection result = mapper.mapTimeDistribution(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("배변 소요 시간은\n주로 10분 이상이었어요");
    assertThat(result.within5min()).isEqualTo(7);
    assertThat(result.over5min()).isEqualTo(9);
    assertThat(result.over10min()).isEqualTo(20);
    assertThat(result.warning()).isEqualTo("소요 시간이 10분이 넘으면 변비 · 치질 위험도가 올라가요");
  }

  @Test
  @DisplayName("모든 시간대가 동일한 빈도일 때 올바른 메시지를 반환한다")
  void givenAllTimesEqual_whenMapTimeDistribution_thenReturnVariedMessage() {
    // given
    ToiletTimeDistribution distribution = new ToiletTimeDistribution(10, 10, 10);
    when(mockReport.getToiletTimeDistribution()).thenReturn(distribution);

    // when
    MonthlyTimeDistributionSection result = mapper.mapTimeDistribution(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("이번 달은 화장실에서\n보낸 시간이 매번 달랐어요");
    assertThat(result.within5min()).isEqualTo(10);
    assertThat(result.over5min()).isEqualTo(10);
    assertThat(result.over10min()).isEqualTo(10);
    assertThat(result.warning()).isEqualTo("소요 시간이 10분이 넘으면 변비 · 치질 위험도가 올라가요");
  }

  @Test
  @DisplayName("두 개의 시간대가 동일한 빈도일 때 올바른 메시지를 반환한다")
  void givenTwoTimesEqual_whenMapTimeDistribution_thenReturnMixedMessage() {
    // given
    ToiletTimeDistribution distribution = new ToiletTimeDistribution(12, 12, 5);
    when(mockReport.getToiletTimeDistribution()).thenReturn(distribution);

    // when
    MonthlyTimeDistributionSection result = mapper.mapTimeDistribution(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("이번 달은 화장실에서\n짧고 긴 시간 모두를 경험했어요");
    assertThat(result.within5min()).isEqualTo(12);
    assertThat(result.over5min()).isEqualTo(12);
    assertThat(result.over10min()).isEqualTo(5);
    assertThat(result.warning()).isNull();
  }

  @Test
  @DisplayName("5분 이내와 10분 이상이 동일한 빈도일 때 올바른 메시지를 반환한다")
  void givenWithin5AndOver10Equal_whenMapTimeDistribution_thenReturnMixedMessage() {
    // given
    ToiletTimeDistribution distribution = new ToiletTimeDistribution(15, 8, 15);
    when(mockReport.getToiletTimeDistribution()).thenReturn(distribution);

    // when
    MonthlyTimeDistributionSection result = mapper.mapTimeDistribution(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("이번 달은 화장실에서\n짧고 긴 시간 모두를 경험했어요");
    assertThat(result.within5min()).isEqualTo(15);
    assertThat(result.over5min()).isEqualTo(8);
    assertThat(result.over10min()).isEqualTo(15);
    assertThat(result.warning()).isEqualTo("소요 시간이 10분이 넘으면 변비 · 치질 위험도가 올라가요");
  }

  @Test
  @DisplayName("5분 이상과 10분 이상이 동일한 빈도일 때 올바른 메시지를 반환한다")
  void givenOver5AndOver10Equal_whenMapTimeDistribution_thenReturnMixedMessage() {
    // given
    ToiletTimeDistribution distribution = new ToiletTimeDistribution(7, 14, 14);
    when(mockReport.getToiletTimeDistribution()).thenReturn(distribution);

    // when
    MonthlyTimeDistributionSection result = mapper.mapTimeDistribution(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("이번 달은 화장실에서\n짧고 긴 시간 모두를 경험했어요");
    assertThat(result.within5min()).isEqualTo(7);
    assertThat(result.over5min()).isEqualTo(14);
    assertThat(result.over10min()).isEqualTo(14);
    assertThat(result.warning()).isEqualTo("소요 시간이 10분이 넘으면 변비 · 치질 위험도가 올라가요");
  }

  @Test
  @DisplayName("5분 이내만 있을 때 올바르게 처리한다")
  void givenOnlyWithin5Min_whenMapTimeDistribution_thenReturnCorrectly() {
    // given
    ToiletTimeDistribution distribution = new ToiletTimeDistribution(30, 0, 0);
    when(mockReport.getToiletTimeDistribution()).thenReturn(distribution);

    // when
    MonthlyTimeDistributionSection result = mapper.mapTimeDistribution(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("배변 소요 시간은\n주로 5분 이내였어요");
    assertThat(result.within5min()).isEqualTo(30);
    assertThat(result.over5min()).isEqualTo(0);
    assertThat(result.over10min()).isEqualTo(0);
    assertThat(result.warning()).isNull();
  }

  @Test
  @DisplayName("10분 이상만 있을 때 경고 메시지를 포함한다")
  void givenOnlyOver10Min_whenMapTimeDistribution_thenReturnWithWarning() {
    // given
    ToiletTimeDistribution distribution = new ToiletTimeDistribution(0, 0, 25);
    when(mockReport.getToiletTimeDistribution()).thenReturn(distribution);

    // when
    MonthlyTimeDistributionSection result = mapper.mapTimeDistribution(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("배변 소요 시간은\n주로 10분 이상이었어요");
    assertThat(result.within5min()).isEqualTo(0);
    assertThat(result.over5min()).isEqualTo(0);
    assertThat(result.over10min()).isEqualTo(25);
    assertThat(result.warning()).isEqualTo("소요 시간이 10분이 넘으면 변비 · 치질 위험도가 올라가요");
  }

  @Test
  @DisplayName("큰 빈도 차이가 있을 때 올바르게 처리한다")
  void givenLargeDifference_whenMapTimeDistribution_thenReturnCorrectly() {
    // given
    ToiletTimeDistribution distribution = new ToiletTimeDistribution(50, 3, 2);
    when(mockReport.getToiletTimeDistribution()).thenReturn(distribution);

    // when
    MonthlyTimeDistributionSection result = mapper.mapTimeDistribution(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("배변 소요 시간은\n주로 5분 이내였어요");
    assertThat(result.within5min()).isEqualTo(50);
    assertThat(result.over5min()).isEqualTo(3);
    assertThat(result.over10min()).isEqualTo(2);
    assertThat(result.warning()).isNull();
  }

  @Test
  @DisplayName("작은 빈도수들로도 올바르게 처리한다")
  void givenSmallNumbers_whenMapTimeDistribution_thenReturnCorrectly() {
    // given
    ToiletTimeDistribution distribution = new ToiletTimeDistribution(1, 2, 3);
    when(mockReport.getToiletTimeDistribution()).thenReturn(distribution);

    // when
    MonthlyTimeDistributionSection result = mapper.mapTimeDistribution(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("배변 소요 시간은\n주로 10분 이상이었어요");
    assertThat(result.within5min()).isEqualTo(1);
    assertThat(result.over5min()).isEqualTo(2);
    assertThat(result.over10min()).isEqualTo(3);
    assertThat(result.warning()).isEqualTo("소요 시간이 10분이 넘으면 변비 · 치질 위험도가 올라가요");
  }

  @Test
  @DisplayName("5분 이내와 5분 이상이 동일하고 10분 이상이 다를 때 올바르게 처리한다")
  void
      givenWithin5AndOver5EqualButOver10Different_whenMapTimeDistribution_thenReturnMixedMessage() {
    // given
    ToiletTimeDistribution distribution = new ToiletTimeDistribution(10, 10, 5);
    when(mockReport.getToiletTimeDistribution()).thenReturn(distribution);

    // when
    MonthlyTimeDistributionSection result = mapper.mapTimeDistribution(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("이번 달은 화장실에서\n짧고 긴 시간 모두를 경험했어요");
    assertThat(result.warning()).isNull();
  }
}
