package depromeet.lessonfour.server.report.api.mapper.toilet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import depromeet.lessonfour.server.common.api.code.ErrorCode;
import depromeet.lessonfour.server.common.exception.ServerException;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyColorSection;
import depromeet.lessonfour.server.report.domain.vo.MonthlyReport;
import depromeet.lessonfour.server.report.domain.vo.toilet.ToiletColorCount;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletColor;

class ToiletReportMapperColorTest {

  private ToiletReportMapper mapper;
  private MonthlyReport mockReport;

  @BeforeEach
  void setUp() {
    mapper = new ToiletReportMapper();
    mockReport = mock(MonthlyReport.class);
  }

  @Test
  @DisplayName("빨간색이 가장 많을 때 혈변 경고 메시지를 포함한다")
  void givenRedColorMostFrequent_whenMapColor_thenReturnWithBloodWarning() {
    // given
    List<ToiletColorCount> colors =
        List.of(
            new ToiletColorCount(ToiletColor.RED, 10),
            new ToiletColorCount(ToiletColor.DARK_BROWN, 5),
            new ToiletColorCount(ToiletColor.GOLD, 3));
    when(mockReport.getMostFrequentToiletColors()).thenReturn(colors);

    // when
    MonthlyColorSection result = mapper.mapColor(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("가장 많이 확인한 색상은\n적색이에요");
    assertThat(result.colorMessage())
        .isEqualTo("혈변은 건강의 적신호예요. 대장염, 대장암, 치질 등의 문제일 수도 있어요. 빠른 병원 방문을 권장해요.");
    assertThat(result.items()).hasSize(3);
    assertThat(result.items().getFirst().color()).isEqualTo("RED");
    assertThat(result.items().getFirst().count()).isEqualTo(10);
  }

  @Test
  @DisplayName("흰색이 가장 많을 때 간/담도 경고 메시지를 포함한다")
  void givenWhiteColorMostFrequent_whenMapColor_thenReturnWithLiverWarning() {
    // given
    List<ToiletColorCount> colors =
        List.of(
            new ToiletColorCount(ToiletColor.WHITE, 8),
            new ToiletColorCount(ToiletColor.DARK_BROWN, 6),
            new ToiletColorCount(ToiletColor.GOLD, 4));
    when(mockReport.getMostFrequentToiletColors()).thenReturn(colors);

    // when
    MonthlyColorSection result = mapper.mapColor(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("가장 많이 확인한 색상은\n흰색이에요");
    assertThat(result.colorMessage())
        .isEqualTo("흰색은 건강의 적신호예요. 간이나 담도가 좋지 않은 상태일 수도 있어요. 빠른 병원 방문을 권장해요.");
    assertThat(result.items()).hasSize(3);
  }

  @Test
  @DisplayName("검은색이 가장 많을 때 위장 경고 메시지를 포함한다")
  void givenBlackColorMostFrequent_whenMapColor_thenReturnWithStomachWarning() {
    // given
    List<ToiletColorCount> colors =
        List.of(
            new ToiletColorCount(ToiletColor.BLACK, 12),
            new ToiletColorCount(ToiletColor.DARK_BROWN, 7),
            new ToiletColorCount(ToiletColor.GOLD, 5));
    when(mockReport.getMostFrequentToiletColors()).thenReturn(colors);

    // when
    MonthlyColorSection result = mapper.mapColor(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("가장 많이 확인한 색상은\n흑색이에요");
    assertThat(result.colorMessage())
        .isEqualTo("흑변은 건강의 적신호예요. 위궤양, 위암 등 위 관련 문제일 수도 있어요. 즉시 병원을 방문하셔야 해요");
    assertThat(result.items()).hasSize(3);
  }

  @Test
  @DisplayName("갈색이 가장 많을 때 경고 메시지가 없다")
  void givenBrownColorMostFrequent_whenMapColor_thenReturnWithoutWarning() {
    // given
    List<ToiletColorCount> colors =
        List.of(
            new ToiletColorCount(ToiletColor.DARK_BROWN, 15),
            new ToiletColorCount(ToiletColor.GOLD, 8),
            new ToiletColorCount(ToiletColor.GREEN, 5));
    when(mockReport.getMostFrequentToiletColors()).thenReturn(colors);

    // when
    MonthlyColorSection result = mapper.mapColor(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("가장 많이 확인한 색상은\n갈색이에요");
    assertThat(result.colorMessage()).isEmpty();
    assertThat(result.items()).hasSize(3);
  }

  @Test
  @DisplayName("노란색이 가장 많을 때 경고 메시지가 없다")
  void givenYellowColorMostFrequent_whenMapColor_thenReturnWithoutWarning() {
    // given
    List<ToiletColorCount> colors =
        List.of(
            new ToiletColorCount(ToiletColor.GOLD, 11),
            new ToiletColorCount(ToiletColor.DARK_BROWN, 9),
            new ToiletColorCount(ToiletColor.GREEN, 6));
    when(mockReport.getMostFrequentToiletColors()).thenReturn(colors);

    // when
    MonthlyColorSection result = mapper.mapColor(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("가장 많이 확인한 색상은\n황금색이에요");
    assertThat(result.colorMessage()).isEmpty();
    assertThat(result.items()).hasSize(3);
  }

  @Test
  @DisplayName("초록색이 가장 많을 때 경고 메시지가 없다")
  void givenGreenColorMostFrequent_whenMapColor_thenReturnWithoutWarning() {
    // given
    List<ToiletColorCount> colors =
        List.of(
            new ToiletColorCount(ToiletColor.GREEN, 10),
            new ToiletColorCount(ToiletColor.DARK_BROWN, 7),
            new ToiletColorCount(ToiletColor.GOLD, 4));
    when(mockReport.getMostFrequentToiletColors()).thenReturn(colors);

    // when
    MonthlyColorSection result = mapper.mapColor(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("가장 많이 확인한 색상은\n녹색이에요");
    assertThat(result.colorMessage()).isEmpty();
    assertThat(result.items()).hasSize(3);
  }

  @Test
  @DisplayName("색상 기록이 없으면 예외를 발생시킨다")
  void givenEmptyColorList_whenMapColor_thenThrowException() {
    // given
    when(mockReport.getMostFrequentToiletColors()).thenReturn(Collections.emptyList());

    // when & then
    assertThatThrownBy(() -> mapper.mapColor(mockReport))
        .isInstanceOf(ServerException.class)
        .hasFieldOrPropertyWithValue("baseErrorCode", ErrorCode.INTERNAL_SERVER_ERROR);
  }

  @Test
  @DisplayName("단일 색상만 있을 때도 올바르게 매핑한다")
  void givenSingleColor_whenMapColor_thenReturnCorrectly() {
    // given
    List<ToiletColorCount> colors = List.of(new ToiletColorCount(ToiletColor.DARK_BROWN, 20));
    when(mockReport.getMostFrequentToiletColors()).thenReturn(colors);

    // when
    MonthlyColorSection result = mapper.mapColor(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("가장 많이 확인한 색상은\n갈색이에요");
    assertThat(result.items()).hasSize(1);
    assertThat(result.items().get(0).color()).isEqualTo("DARK_BROWN");
    assertThat(result.items().get(0).count()).isEqualTo(20);
  }

  @Test
  @DisplayName("여러 색상이 있을 때 모든 색상을 올바르게 매핑한다")
  void givenMultipleColors_whenMapColor_thenReturnAllColorsCorrectly() {
    // given
    List<ToiletColorCount> colors =
        List.of(
            new ToiletColorCount(ToiletColor.DARK_BROWN, 18),
            new ToiletColorCount(ToiletColor.GOLD, 12),
            new ToiletColorCount(ToiletColor.GREEN, 8),
            new ToiletColorCount(ToiletColor.RED, 5),
            new ToiletColorCount(ToiletColor.BLACK, 3));
    when(mockReport.getMostFrequentToiletColors()).thenReturn(colors);

    // when
    MonthlyColorSection result = mapper.mapColor(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.items()).hasSize(5);
    assertThat(result.items().get(0).color()).isEqualTo("DARK_BROWN");
    assertThat(result.items().get(0).count()).isEqualTo(18);
    assertThat(result.items().get(1).color()).isEqualTo("GOLD");
    assertThat(result.items().get(1).count()).isEqualTo(12);
    assertThat(result.items().get(2).color()).isEqualTo("GREEN");
    assertThat(result.items().get(2).count()).isEqualTo(8);
  }

  @Test
  @DisplayName("동일한 빈도수를 가진 색상들이 있을 때 우선순위에 따라 처리한다")
  void givenSameFrequencyColors_whenMapColor_thenReturnByPriority() {
    // given
    List<ToiletColorCount> colors =
        List.of(
            new ToiletColorCount(ToiletColor.DARK_BROWN, 10),
            new ToiletColorCount(ToiletColor.GOLD, 10),
            new ToiletColorCount(ToiletColor.GREEN, 5));
    when(mockReport.getMostFrequentToiletColors()).thenReturn(colors);

    // when
    MonthlyColorSection result = mapper.mapColor(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("가장 많이 확인한 색상은\n황금색이에요");
    assertThat(result.items()).hasSize(3);
    assertThat(result.items().get(0).color()).isEqualTo("GOLD");
    assertThat(result.items().get(1).color()).isEqualTo("DARK_BROWN");
  }

  @Test
  @DisplayName("적색과 흑색이 동률일 때 적색이 우선순위로 선택된다")
  void givenRedAndBlackWithSameCount_whenMapColor_thenReturnRedFirst() {
    // given
    List<ToiletColorCount> colors =
        List.of(
            new ToiletColorCount(ToiletColor.RED, 10),
            new ToiletColorCount(ToiletColor.BLACK, 10),
            new ToiletColorCount(ToiletColor.DARK_BROWN, 5));
    when(mockReport.getMostFrequentToiletColors()).thenReturn(colors);

    // when
    MonthlyColorSection result = mapper.mapColor(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("가장 많이 확인한 색상은\n적색이에요");
    assertThat(result.colorMessage())
        .isEqualTo("혈변은 건강의 적신호예요. 대장염, 대장암, 치질 등의 문제일 수도 있어요. 빠른 병원 방문을 권장해요.");
    assertThat(result.items()).hasSize(3);
    assertThat(result.items().get(0).color()).isEqualTo("RED");
    assertThat(result.items().get(1).color()).isEqualTo("BLACK");
  }

  @Test
  @DisplayName("흑색과 흰색이 동률일 때 흑색이 우선순위로 선택된다")
  void givenBlackAndWhiteWithSameCount_whenMapColor_thenReturnBlackFirst() {
    // given
    List<ToiletColorCount> colors =
        List.of(
            new ToiletColorCount(ToiletColor.BLACK, 12),
            new ToiletColorCount(ToiletColor.WHITE, 12),
            new ToiletColorCount(ToiletColor.GREEN, 6));
    when(mockReport.getMostFrequentToiletColors()).thenReturn(colors);

    // when
    MonthlyColorSection result = mapper.mapColor(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("가장 많이 확인한 색상은\n흑색이에요");
    assertThat(result.colorMessage())
        .isEqualTo("흑변은 건강의 적신호예요. 위궤양, 위암 등 위 관련 문제일 수도 있어요. 즉시 병원을 방문하셔야 해요");
    assertThat(result.items()).hasSize(3);
    assertThat(result.items().get(0).color()).isEqualTo("BLACK");
    assertThat(result.items().get(1).color()).isEqualTo("WHITE");
  }

  @Test
  @DisplayName("흰색과 녹색이 동률일 때 흰색이 우선순위로 선택된다")
  void givenWhiteAndGreenWithSameCount_whenMapColor_thenReturnWhiteFirst() {
    // given
    List<ToiletColorCount> colors =
        List.of(
            new ToiletColorCount(ToiletColor.WHITE, 8),
            new ToiletColorCount(ToiletColor.GREEN, 8),
            new ToiletColorCount(ToiletColor.DARK_BROWN, 4));
    when(mockReport.getMostFrequentToiletColors()).thenReturn(colors);

    // when
    MonthlyColorSection result = mapper.mapColor(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("가장 많이 확인한 색상은\n흰색이에요");
    assertThat(result.colorMessage())
        .isEqualTo("흰색은 건강의 적신호예요. 간이나 담도가 좋지 않은 상태일 수도 있어요. 빠른 병원 방문을 권장해요.");
    assertThat(result.items()).hasSize(3);
    assertThat(result.items().get(0).color()).isEqualTo("WHITE");
    assertThat(result.items().get(1).color()).isEqualTo("GREEN");
  }

  @Test
  @DisplayName("녹색과 황금색이 동률일 때 녹색이 우선순위로 선택된다")
  void givenGreenAndGoldWithSameCount_whenMapColor_thenReturnGreenFirst() {
    // given
    List<ToiletColorCount> colors =
        List.of(
            new ToiletColorCount(ToiletColor.GREEN, 15),
            new ToiletColorCount(ToiletColor.GOLD, 15),
            new ToiletColorCount(ToiletColor.DARK_BROWN, 10));
    when(mockReport.getMostFrequentToiletColors()).thenReturn(colors);

    // when
    MonthlyColorSection result = mapper.mapColor(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("가장 많이 확인한 색상은\n녹색이에요");
    assertThat(result.colorMessage()).isEmpty();
    assertThat(result.items()).hasSize(3);
    assertThat(result.items().get(0).color()).isEqualTo("GREEN");
    assertThat(result.items().get(1).color()).isEqualTo("GOLD");
  }

  @Test
  @DisplayName("황금색과 갈색이 동률일 때 황금색이 우선순위로 선택된다")
  void givenGoldAndBrownWithSameCount_whenMapColor_thenReturnGoldFirst() {
    // given
    List<ToiletColorCount> colors =
        List.of(
            new ToiletColorCount(ToiletColor.GOLD, 20),
            new ToiletColorCount(ToiletColor.DARK_BROWN, 20),
            new ToiletColorCount(ToiletColor.GREEN, 15));
    when(mockReport.getMostFrequentToiletColors()).thenReturn(colors);

    // when
    MonthlyColorSection result = mapper.mapColor(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("가장 많이 확인한 색상은\n황금색이에요");
    assertThat(result.colorMessage()).isEmpty();
    assertThat(result.items()).hasSize(3);
    assertThat(result.items().get(0).color()).isEqualTo("GOLD");
    assertThat(result.items().get(1).color()).isEqualTo("DARK_BROWN");
  }

  @Test
  @DisplayName("모든 색상이 동률일 때 우선순위에 따라 적색이 먼저 선택된다")
  void givenAllColorsWithSameCount_whenMapColor_thenReturnInPriorityOrder() {
    // given
    List<ToiletColorCount> colors =
        List.of(
            new ToiletColorCount(ToiletColor.RED, 10),
            new ToiletColorCount(ToiletColor.BLACK, 10),
            new ToiletColorCount(ToiletColor.WHITE, 10),
            new ToiletColorCount(ToiletColor.GREEN, 10),
            new ToiletColorCount(ToiletColor.GOLD, 10),
            new ToiletColorCount(ToiletColor.DARK_BROWN, 10));
    when(mockReport.getMostFrequentToiletColors()).thenReturn(colors);

    // when
    MonthlyColorSection result = mapper.mapColor(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("가장 많이 확인한 색상은\n적색이에요");
    assertThat(result.colorMessage())
        .isEqualTo("혈변은 건강의 적신호예요. 대장염, 대장암, 치질 등의 문제일 수도 있어요. 빠른 병원 방문을 권장해요.");
    assertThat(result.items()).hasSize(6);
    // 우선순위 순서: 적색, 흑색, 흰색, 녹색, 황금색, 갈색
    assertThat(result.items().get(0).color()).isEqualTo("RED");
    assertThat(result.items().get(1).color()).isEqualTo("BLACK");
    assertThat(result.items().get(2).color()).isEqualTo("WHITE");
    assertThat(result.items().get(3).color()).isEqualTo("GREEN");
    assertThat(result.items().get(4).color()).isEqualTo("GOLD");
    assertThat(result.items().get(5).color()).isEqualTo("DARK_BROWN");
  }

  @Test
  @DisplayName("적색과 갈색이 동률일 때 적색이 우선순위로 선택된다")
  void givenRedAndBrownWithSameCount_whenMapColor_thenReturnRedFirst() {
    // given
    List<ToiletColorCount> colors =
        List.of(
            new ToiletColorCount(ToiletColor.RED, 15),
            new ToiletColorCount(ToiletColor.DARK_BROWN, 15),
            new ToiletColorCount(ToiletColor.GOLD, 10));
    when(mockReport.getMostFrequentToiletColors()).thenReturn(colors);

    // when
    MonthlyColorSection result = mapper.mapColor(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("가장 많이 확인한 색상은\n적색이에요");
    assertThat(result.colorMessage())
        .isEqualTo("혈변은 건강의 적신호예요. 대장염, 대장암, 치질 등의 문제일 수도 있어요. 빠른 병원 방문을 권장해요.");
    assertThat(result.items()).hasSize(3);
    assertThat(result.items().get(0).color()).isEqualTo("RED");
    assertThat(result.items().get(1).color()).isEqualTo("DARK_BROWN");
    assertThat(result.items().get(0).count()).isEqualTo(15);
    assertThat(result.items().get(1).count()).isEqualTo(15);
  }
}
