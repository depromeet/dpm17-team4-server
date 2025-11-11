package depromeet.lessonfour.server.report.api.mapper.toilet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyShapeSection;
import depromeet.lessonfour.server.report.domain.vo.MonthlyReport;
import depromeet.lessonfour.server.report.domain.vo.toilet.ToiletShapeCount;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletShape;

class ToiletReportMapperShapeTest {

  private ToiletReportMapper mapper;
  private MonthlyReport mockReport;

  @BeforeEach
  void setUp() {
    mapper = new ToiletReportMapper();
    mockReport = mock(MonthlyReport.class);
  }

  @Test
  @DisplayName("토끼똥 모양이 가장 많을 때 '변비 주의' 메시지를 포함한다")
  void givenRabbitShapeMostFrequent_whenMapShape_thenReturnWithConstipationWarning() {
    // given
    List<ToiletShapeCount> shapes =
        List.of(
            new ToiletShapeCount(ToiletShape.RABBIT, 10),
            new ToiletShapeCount(ToiletShape.BANANA, 5),
            new ToiletShapeCount(ToiletShape.CORN, 3));
    when(mockReport.getMostFrequentToiletShapes()).thenReturn(shapes);

    // when
    MonthlyShapeSection result = mapper.mapShape(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("이번 달 자주 본 배변 모양이에요");
    assertThat(result.items()).hasSize(3);
    assertThat(result.items().get(0).shape()).isEqualTo("RABBIT");
    assertThat(result.items().get(0).count()).isEqualTo(10);
    assertThat(result.items().get(0).message()).isEqualTo("변비 주의");
  }

  @Test
  @DisplayName("바나나 모양이 가장 많을 때 빈 메시지를 포함한다")
  void givenBananaShapeMostFrequent_whenMapShape_thenReturnWithEmptyMessage() {
    // given
    List<ToiletShapeCount> shapes =
        List.of(
            new ToiletShapeCount(ToiletShape.BANANA, 12),
            new ToiletShapeCount(ToiletShape.CORN, 7),
            new ToiletShapeCount(ToiletShape.RABBIT, 5));
    when(mockReport.getMostFrequentToiletShapes()).thenReturn(shapes);

    // when
    MonthlyShapeSection result = mapper.mapShape(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.items().get(0).shape()).isEqualTo("BANANA");
    assertThat(result.items().get(0).count()).isEqualTo(12);
    assertThat(result.items().get(0).message()).isEmpty();
  }

  @Test
  @DisplayName("옥수수 모양이 가장 많을 때 '수분 충전 필요' 메시지를 포함한다")
  void givenCornShapeMostFrequent_whenMapShape_thenReturnWithHydrationWarning() {
    // given
    List<ToiletShapeCount> shapes =
        List.of(
            new ToiletShapeCount(ToiletShape.CORN, 15),
            new ToiletShapeCount(ToiletShape.BANANA, 8),
            new ToiletShapeCount(ToiletShape.RABBIT, 6));
    when(mockReport.getMostFrequentToiletShapes()).thenReturn(shapes);

    // when
    MonthlyShapeSection result = mapper.mapShape(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.items().get(0).shape()).isEqualTo("CORN");
    assertThat(result.items().get(0).count()).isEqualTo(15);
    assertThat(result.items().get(0).message()).isEqualTo("수분 충전 필요");
  }

  @Test
  @DisplayName("크림 모양이 가장 많을 때 '설사 주의' 메시지를 포함한다")
  void givenCreamShapeMostFrequent_whenMapShape_thenReturnWithDiarrheaWarning() {
    // given
    List<ToiletShapeCount> shapes =
        List.of(
            new ToiletShapeCount(ToiletShape.CREAM, 11),
            new ToiletShapeCount(ToiletShape.BANANA, 6),
            new ToiletShapeCount(ToiletShape.CORN, 4));
    when(mockReport.getMostFrequentToiletShapes()).thenReturn(shapes);

    // when
    MonthlyShapeSection result = mapper.mapShape(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.items().get(0).shape()).isEqualTo("CREAM");
    assertThat(result.items().get(0).count()).isEqualTo(11);
    assertThat(result.items().get(0).message()).isEqualTo("설사 주의");
  }

  @Test
  @DisplayName("죽 모양이 가장 많을 때 '설사 주의' 메시지를 포함한다")
  void givenPorridgeShapeMostFrequent_whenMapShape_thenReturnWithDiarrheaWarning() {
    // given
    List<ToiletShapeCount> shapes =
        List.of(
            new ToiletShapeCount(ToiletShape.PORRIDGE, 9),
            new ToiletShapeCount(ToiletShape.BANANA, 5),
            new ToiletShapeCount(ToiletShape.CORN, 3));
    when(mockReport.getMostFrequentToiletShapes()).thenReturn(shapes);

    // when
    MonthlyShapeSection result = mapper.mapShape(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.items().get(0).shape()).isEqualTo("PORRIDGE");
    assertThat(result.items().get(0).count()).isEqualTo(9);
    assertThat(result.items().get(0).message()).isEqualTo("설사 주의");
  }

  @Test
  @DisplayName("물 모양이 가장 많을 때 '설사 주의' 메시지를 포함한다")
  void givenWaterShapeMostFrequent_whenMapShape_thenReturnWithDiarrheaWarning() {
    // given
    List<ToiletShapeCount> shapes =
        List.of(
            new ToiletShapeCount(ToiletShape.WATER, 13),
            new ToiletShapeCount(ToiletShape.BANANA, 7),
            new ToiletShapeCount(ToiletShape.CORN, 5));
    when(mockReport.getMostFrequentToiletShapes()).thenReturn(shapes);

    // when
    MonthlyShapeSection result = mapper.mapShape(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.items().get(0).shape()).isEqualTo("WATER");
    assertThat(result.items().get(0).count()).isEqualTo(13);
    assertThat(result.items().get(0).message()).isEqualTo("설사 주의");
  }

  @Test
  @DisplayName("바위 모양이 가장 많을 때 빈 메시지를 포함한다")
  void givenRockShapeMostFrequent_whenMapShape_thenReturnWithEmptyMessage() {
    // given
    List<ToiletShapeCount> shapes =
        List.of(
            new ToiletShapeCount(ToiletShape.ROCK, 8),
            new ToiletShapeCount(ToiletShape.BANANA, 6),
            new ToiletShapeCount(ToiletShape.CORN, 4));
    when(mockReport.getMostFrequentToiletShapes()).thenReturn(shapes);

    // when
    MonthlyShapeSection result = mapper.mapShape(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.items().get(0).shape()).isEqualTo("ROCK");
    assertThat(result.items().get(0).count()).isEqualTo(8);
    assertThat(result.items().get(0).message()).isEmpty();
  }

  @Test
  @DisplayName("여러 모양이 있을 때 모든 모양을 올바르게 매핑한다")
  void givenMultipleShapes_whenMapShape_thenReturnAllShapesCorrectly() {
    // given
    List<ToiletShapeCount> shapes =
        List.of(
            new ToiletShapeCount(ToiletShape.BANANA, 15),
            new ToiletShapeCount(ToiletShape.RABBIT, 12),
            new ToiletShapeCount(ToiletShape.CORN, 10),
            new ToiletShapeCount(ToiletShape.CREAM, 8),
            new ToiletShapeCount(ToiletShape.WATER, 5));
    when(mockReport.getMostFrequentToiletShapes()).thenReturn(shapes);

    // when
    MonthlyShapeSection result = mapper.mapShape(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.titleMessage()).isEqualTo("이번 달 자주 본 배변 모양이에요");
    assertThat(result.items()).hasSize(5);
    assertThat(result.items().get(0).shape()).isEqualTo("BANANA");
    assertThat(result.items().get(0).message()).isEmpty();
    assertThat(result.items().get(1).shape()).isEqualTo("RABBIT");
    assertThat(result.items().get(1).message()).isEqualTo("변비 주의");
    assertThat(result.items().get(2).shape()).isEqualTo("CORN");
    assertThat(result.items().get(2).message()).isEqualTo("수분 충전 필요");
    assertThat(result.items().get(3).shape()).isEqualTo("CREAM");
    assertThat(result.items().get(3).message()).isEqualTo("설사 주의");
    assertThat(result.items().get(4).shape()).isEqualTo("WATER");
    assertThat(result.items().get(4).message()).isEqualTo("설사 주의");
  }

  @Test
  @DisplayName("단일 모양만 있을 때도 올바르게 매핑한다")
  void givenSingleShape_whenMapShape_thenReturnCorrectly() {
    // given
    List<ToiletShapeCount> shapes = List.of(new ToiletShapeCount(ToiletShape.BANANA, 20));
    when(mockReport.getMostFrequentToiletShapes()).thenReturn(shapes);

    // when
    MonthlyShapeSection result = mapper.mapShape(mockReport);

    // then
    assertThat(result).isNotNull();
    assertThat(result.items()).hasSize(1);
    assertThat(result.items().get(0).shape()).isEqualTo("BANANA");
    assertThat(result.items().get(0).count()).isEqualTo(20);
  }
}
