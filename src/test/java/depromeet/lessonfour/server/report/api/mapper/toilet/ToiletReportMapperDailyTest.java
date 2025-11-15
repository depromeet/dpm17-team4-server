package depromeet.lessonfour.server.report.api.mapper.toilet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.report.api.dto.response.GetDailyReportResponseDto.DailyToiletReportDto;
import depromeet.lessonfour.server.report.domain.vo.toilet.DailyToiletReport;
import depromeet.lessonfour.server.report.domain.vo.toilet.ToiletEvaluation;
import depromeet.lessonfour.server.report.domain.vo.toilet.ToiletEvaluationLevel;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletColor;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletShape;

class ToiletReportMapperDailyTest {

  private ToiletReportMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new ToiletReportMapper();
  }

  @Test
  @DisplayName("VERY_GOOD 레벨의 일간 리포트를 올바르게 매핑한다")
  void givenVeryGoodLevel_whenMapDaily_thenReturnCorrectReport() {
    // given
    ToiletEvaluation mockItem =
        createMockToiletEvaluation(
            ActivityAt.of(LocalDate.of(2024, 1, 1)),
            ToiletColor.DARK_BROWN,
            ToiletShape.BANANA,
            5,
            1,
            "test note");
    DailyToiletReport dailyReport =
        createDailyReport(ToiletEvaluationLevel.VERY_GOOD, 95, List.of(mockItem));

    // when
    DailyToiletReportDto result = mapper.mapDaily(dailyReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.score()).isEqualTo(95);
    assertThat(result.summary().image())
        .isEqualTo(
            "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/toilet/very_good.png");
    assertThat(result.summary().backgroundColors()).containsExactly("#0C7C30", "#7DD357");
    assertThat(result.summary().caption()).isEqualTo("신이 난 대장");
    assertThat(result.summary().message()).isEqualTo("장 컨디션 아주 굿!");
    assertThat(result.items()).hasSize(1);
  }

  @Test
  @DisplayName("GOOD 레벨의 일간 리포트를 올바르게 매핑한다")
  void givenGoodLevel_whenMapDaily_thenReturnCorrectReport() {
    // given
    ToiletEvaluation mockItem =
        createMockToiletEvaluation(
            ActivityAt.of(LocalDate.of(2024, 1, 1)),
            ToiletColor.DARK_BROWN,
            ToiletShape.BANANA,
            5,
            2,
            null);
    DailyToiletReport dailyReport =
        createDailyReport(ToiletEvaluationLevel.GOOD, 80, List.of(mockItem));

    // when
    DailyToiletReportDto result = mapper.mapDaily(dailyReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.score()).isEqualTo(80);
    assertThat(result.summary().image())
        .isEqualTo(
            "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/toilet/good.png");
    assertThat(result.summary().backgroundColors()).containsExactly("#134DB1", "#588DFF");
    assertThat(result.summary().caption()).isEqualTo("기분 좋은 대장");
    assertThat(result.summary().message()).isEqualTo("개운하실 것 같아요!");
  }

  @Test
  @DisplayName("AVERAGE 레벨의 일간 리포트를 올바르게 매핑한다")
  void givenAverageLevel_whenMapDaily_thenReturnCorrectReport() {
    // given
    ToiletEvaluation mockItem =
        createMockToiletEvaluation(
            ActivityAt.of(LocalDate.of(2024, 1, 1)),
            ToiletColor.DARK_BROWN,
            ToiletShape.CORN,
            5,
            3,
            "average");
    DailyToiletReport dailyReport =
        createDailyReport(ToiletEvaluationLevel.AVERAGE, 60, List.of(mockItem));

    // when
    DailyToiletReportDto result = mapper.mapDaily(dailyReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.score()).isEqualTo(60);
    assertThat(result.summary().image())
        .isEqualTo(
            "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/toilet/normal.png");
    assertThat(result.summary().backgroundColors()).containsExactly("#2B42B4", "#8F58FF");
    assertThat(result.summary().caption()).isEqualTo("얌전한 대장");
    assertThat(result.summary().message()).isEqualTo("무난한 하루가 되었군요!");
  }

  @Test
  @DisplayName("BAD 레벨의 일간 리포트를 올바르게 매핑한다")
  void givenBadLevel_whenMapDaily_thenReturnCorrectReport() {
    // given
    ToiletEvaluation mockItem =
        createMockToiletEvaluation(
            ActivityAt.of(LocalDate.of(2024, 1, 1)),
            ToiletColor.DARK_BROWN,
            ToiletShape.WATER,
            5,
            4,
            null);
    DailyToiletReport dailyReport =
        createDailyReport(ToiletEvaluationLevel.BAD, 40, List.of(mockItem));

    // when
    DailyToiletReportDto result = mapper.mapDaily(dailyReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.score()).isEqualTo(40);
    assertThat(result.summary().image())
        .isEqualTo(
            "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/toilet/bad.png");
    assertThat(result.summary().backgroundColors()).containsExactly("#DD5612", "#F6A85F");
    assertThat(result.summary().caption()).isEqualTo("속상한 대장");
    assertThat(result.summary().message()).isEqualTo("잠시 관리가 필요해요!");
  }

  @Test
  @DisplayName("VERY_BAD 레벨의 일간 리포트를 올바르게 매핑한다")
  void givenVeryBadLevel_whenMapDaily_thenReturnCorrectReport() {
    // given
    ToiletEvaluation mockItem =
        createMockToiletEvaluation(
            ActivityAt.of(LocalDate.of(2024, 1, 1)),
            ToiletColor.RED,
            ToiletShape.WATER,
            10,
            5,
            "very bad");
    DailyToiletReport dailyReport =
        createDailyReport(ToiletEvaluationLevel.VERY_BAD, 20, List.of(mockItem));

    // when
    DailyToiletReportDto result = mapper.mapDaily(dailyReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.score()).isEqualTo(20);
    assertThat(result.summary().image())
        .isEqualTo(
            "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/toilet/very_bad.png");
    assertThat(result.summary().backgroundColors()).containsExactly("#A4141E", "#FF535F");
    assertThat(result.summary().caption()).isEqualTo("화가 잔뜩 난 대장");
    assertThat(result.summary().message()).isEqualTo("전문가 상담이 필요해요!");
  }

  @Test
  @DisplayName("NONE 레벨의 일간 리포트는 null을 반환한다")
  void givenNoneLevel_whenMapDaily_thenReturnNull() {
    // given
    DailyToiletReport dailyReport = createDailyReport(ToiletEvaluationLevel.NONE, 0, List.of());

    // when
    DailyToiletReportDto result = mapper.mapDaily(dailyReport);

    // then
    assertThat(result).isNull();
  }

  @Test
  @DisplayName("여러 기록 아이템이 있는 일간 리포트를 올바르게 매핑한다")
  void givenMultipleItems_whenMapDaily_thenReturnCorrectReport() {
    // given
    ToiletEvaluation item1 =
        createMockToiletEvaluation(
            ActivityAt.of(LocalDate.of(2024, 1, 1)),
            ToiletColor.DARK_BROWN,
            ToiletShape.BANANA,
            5,
            1,
            "morning");
    ToiletEvaluation item2 =
        createMockToiletEvaluation(
            ActivityAt.of(LocalDate.of(2024, 1, 1)),
            ToiletColor.DARK_BROWN,
            ToiletShape.CORN,
            4,
            2,
            "afternoon");
    DailyToiletReport dailyReport =
        createDailyReport(ToiletEvaluationLevel.GOOD, 85, List.of(item1, item2));

    // when
    DailyToiletReportDto result = mapper.mapDaily(dailyReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.items()).hasSize(2);
    assertThat(result.items().get(0).note()).isEqualTo("morning");
    assertThat(result.items().get(1).note()).isEqualTo("afternoon");
  }

  // Helper methods
  private ToiletEvaluation createMockToiletEvaluation(
      ActivityAt occurredAt,
      ToiletColor color,
      ToiletShape shape,
      int duration,
      double pain,
      String note) {
    ToiletEvaluation mock = mock(ToiletEvaluation.class);
    when(mock.getOccurredAt()).thenReturn(occurredAt);
    when(mock.getColor()).thenReturn(color);
    when(mock.getShape()).thenReturn(shape);
    when(mock.getDuration()).thenReturn(duration);
    when(mock.getPain()).thenReturn(pain);
    when(mock.getNote()).thenReturn(note);
    return mock;
  }

  private DailyToiletReport createDailyReport(
      ToiletEvaluationLevel level, double score, List<ToiletEvaluation> items) {
    DailyToiletReport mock = mock(DailyToiletReport.class);
    when(mock.getLevel()).thenReturn(level);
    when(mock.getToiletScore()).thenReturn(score);
    when(mock.getItems()).thenReturn(items);
    return mock;
  }
}
