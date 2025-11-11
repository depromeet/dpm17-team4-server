package depromeet.lessonfour.server.report.api.mapper.toilet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyPeriodSection;
import depromeet.lessonfour.server.report.domain.vo.MonthlyReport;
import depromeet.lessonfour.server.report.domain.vo.toilet.DayPeriod;
import depromeet.lessonfour.server.report.domain.vo.toilet.ToiletPeriodCount;

class ToiletReportMapperPeriodTest {

  private ToiletReportMapper mapper;
  private MonthlyReport mockReport;

  @BeforeEach
  void setUp() {
    mapper = new ToiletReportMapper();
    mockReport = mock(MonthlyReport.class);
  }

  @Test
  @DisplayName("아침 시간대가 가장 많을 때 올바르게 매핑한다")
  void givenMorningMostFrequent_whenMapPeriodSection_thenReturnCorrectSection() {
    // given
    List<ToiletPeriodCount> periods =
        List.of(
            new ToiletPeriodCount(DayPeriod.MORNING, 15),
            new ToiletPeriodCount(DayPeriod.AFTERNOON, 8),
            new ToiletPeriodCount(DayPeriod.EVENING, 7));
    when(mockReport.getToiletPeriodCounts()).thenReturn(periods);

    // when
    MonthlyPeriodSection result = mapper.mapPeriodSection(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("이번 달은 주로\n오전에 성공했어요");
    assertThat(result.items()).hasSize(3);
    assertThat(result.items().get(0).period()).isEqualTo("오전");
    assertThat(result.items().get(0).count()).isEqualTo(15);
  }

  @Test
  @DisplayName("오후 시간대가 가장 많을 때 올바르게 매핑한다")
  void givenAfternoonMostFrequent_whenMapPeriodSection_thenReturnCorrectSection() {
    // given
    List<ToiletPeriodCount> periods =
        List.of(
            new ToiletPeriodCount(DayPeriod.AFTERNOON, 18),
            new ToiletPeriodCount(DayPeriod.MORNING, 10),
            new ToiletPeriodCount(DayPeriod.EVENING, 6));
    when(mockReport.getToiletPeriodCounts()).thenReturn(periods);

    // when
    MonthlyPeriodSection result = mapper.mapPeriodSection(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("이번 달은 주로\n오후에 성공했어요");
    assertThat(result.items()).hasSize(3);
    assertThat(result.items().get(0).period()).isEqualTo("오후");
    assertThat(result.items().get(0).count()).isEqualTo(18);
  }

  @Test
  @DisplayName("저녁 시간대가 가장 많을 때 올바르게 매핑한다")
  void givenEveningMostFrequent_whenMapPeriodSection_thenReturnCorrectSection() {
    // given
    List<ToiletPeriodCount> periods =
        List.of(
            new ToiletPeriodCount(DayPeriod.EVENING, 20),
            new ToiletPeriodCount(DayPeriod.MORNING, 9),
            new ToiletPeriodCount(DayPeriod.AFTERNOON, 7));
    when(mockReport.getToiletPeriodCounts()).thenReturn(periods);

    // when
    MonthlyPeriodSection result = mapper.mapPeriodSection(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("이번 달은 주로\n저녁에 성공했어요");
    assertThat(result.items()).hasSize(3);
    assertThat(result.items().get(0).period()).isEqualTo("저녁");
    assertThat(result.items().get(0).count()).isEqualTo(20);
  }

  @Test
  @DisplayName("가장 많은 시간대와 두 번째 시간대가 동일한 빈도일 때 올바르게 매핑한다")
  void givenTwoPeriodsWithSameFrequency_whenMapPeriodSection_thenReturnBothInTitle() {
    // given
    List<ToiletPeriodCount> periods =
        List.of(
            new ToiletPeriodCount(DayPeriod.MORNING, 12),
            new ToiletPeriodCount(DayPeriod.AFTERNOON, 12),
            new ToiletPeriodCount(DayPeriod.EVENING, 6));
    when(mockReport.getToiletPeriodCounts()).thenReturn(periods);

    // when
    MonthlyPeriodSection result = mapper.mapPeriodSection(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("이번 달은 주로\n오전,오후에 성공했어요");
    assertThat(result.items()).hasSize(3);
  }

  @Test
  @DisplayName("가장 많은 시간대와 마지막 시간대가 동일한 빈도일 때 올바르게 매핑한다")
  void givenFirstAndLastPeriodsWithSameFrequency_whenMapPeriodSection_thenReturnBothInTitle() {
    // given
    List<ToiletPeriodCount> periods =
        List.of(
            new ToiletPeriodCount(DayPeriod.MORNING, 10),
            new ToiletPeriodCount(DayPeriod.AFTERNOON, 5),
            new ToiletPeriodCount(DayPeriod.EVENING, 10));
    when(mockReport.getToiletPeriodCounts()).thenReturn(periods);

    // when
    MonthlyPeriodSection result = mapper.mapPeriodSection(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("이번 달은 주로\n오전,저녁에 성공했어요");
    assertThat(result.items()).hasSize(3);
  }

  @Test
  @DisplayName("모든 시간대가 동일한 빈도일 때 올바르게 매핑한다")
  void givenAllPeriodsWithSameFrequency_whenMapPeriodSection_thenReturnAllInTitle() {
    // given
    List<ToiletPeriodCount> periods =
        List.of(
            new ToiletPeriodCount(DayPeriod.MORNING, 10),
            new ToiletPeriodCount(DayPeriod.AFTERNOON, 10),
            new ToiletPeriodCount(DayPeriod.EVENING, 10));
    when(mockReport.getToiletPeriodCounts()).thenReturn(periods);

    // when
    MonthlyPeriodSection result = mapper.mapPeriodSection(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("이번 달은 주로\n오전,오후,저녁에 성공했어요");
    assertThat(result.items()).hasSize(3);
  }

  @Test
  @DisplayName("시간대별 빈도가 크게 차이날 때 올바르게 매핑한다")
  void givenLargeDifferenceInFrequency_whenMapPeriodSection_thenReturnCorrectly() {
    // given
    List<ToiletPeriodCount> periods =
        List.of(
            new ToiletPeriodCount(DayPeriod.MORNING, 25),
            new ToiletPeriodCount(DayPeriod.AFTERNOON, 3),
            new ToiletPeriodCount(DayPeriod.EVENING, 2));
    when(mockReport.getToiletPeriodCounts()).thenReturn(periods);

    // when
    MonthlyPeriodSection result = mapper.mapPeriodSection(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("이번 달은 주로\n오전에 성공했어요");
    assertThat(result.items()).hasSize(3);
    assertThat(result.items().get(0).count()).isEqualTo(25);
    assertThat(result.items().get(1).count()).isEqualTo(3);
    assertThat(result.items().get(2).count()).isEqualTo(2);
  }

  @Test
  @DisplayName("단일 시간대만 있을 때 올바르게 매핑한다")
  void givenSinglePeriod_whenMapPeriodSection_thenReturnCorrectly() {
    // given
    List<ToiletPeriodCount> periods = List.of(new ToiletPeriodCount(DayPeriod.MORNING, 30));
    when(mockReport.getToiletPeriodCounts()).thenReturn(periods);

    // when
    MonthlyPeriodSection result = mapper.mapPeriodSection(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).contains("오전");
    assertThat(result.items()).hasSize(1);
    assertThat(result.items().get(0).period()).isEqualTo("오전");
    assertThat(result.items().get(0).count()).isEqualTo(30);
  }

  @Test
  @DisplayName("두 개의 시간대만 있을 때 올바르게 매핑한다")
  void givenTwoPeriods_whenMapPeriodSection_thenReturnCorrectly() {
    // given
    List<ToiletPeriodCount> periods =
        List.of(
            new ToiletPeriodCount(DayPeriod.MORNING, 18),
            new ToiletPeriodCount(DayPeriod.AFTERNOON, 12));
    when(mockReport.getToiletPeriodCounts()).thenReturn(periods);

    // when
    MonthlyPeriodSection result = mapper.mapPeriodSection(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("이번 달은 주로\n오전에 성공했어요");
    assertThat(result.items()).hasSize(2);
  }

  @Test
  @DisplayName("빈도가 0인 시간대가 있을 때 올바르게 매핑한다")
  void givenPeriodWithZeroCount_whenMapPeriodSection_thenReturnCorrectly() {
    // given
    List<ToiletPeriodCount> periods =
        List.of(
            new ToiletPeriodCount(DayPeriod.MORNING, 20),
            new ToiletPeriodCount(DayPeriod.AFTERNOON, 0),
            new ToiletPeriodCount(DayPeriod.EVENING, 10));
    when(mockReport.getToiletPeriodCounts()).thenReturn(periods);

    // when
    MonthlyPeriodSection result = mapper.mapPeriodSection(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("이번 달은 주로\n오전에 성공했어요");
    assertThat(result.items()).hasSize(3);
    assertThat(result.items().get(0).count()).isEqualTo(20);
    assertThat(result.items().get(1).count()).isEqualTo(0);
    assertThat(result.items().get(2).count()).isEqualTo(10);
  }
}
