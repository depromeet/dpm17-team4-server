// package depromeet.lessonfour.server.report.api.mapper;
//
// import static org.assertj.core.api.Assertions.assertThat;
//
// import java.util.Collections;
// import java.util.List;
//
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Nested;
// import org.junit.jupiter.api.Test;
//
// import depromeet.lessonfour.server.report.api.dto.response.GetDailyReportResponseDto.WaterReport;
// import
// depromeet.lessonfour.server.report.api.dto.response.GetDailyReportResponseDto.WaterReportItem;
// import depromeet.lessonfour.server.report.domain.vo.activity.DayType;
// import depromeet.lessonfour.server.report.domain.vo.activity.WaterEvaluation;
// import depromeet.lessonfour.server.report.domain.vo.activity.WaterLevel;
//
// @DisplayName("WaterMapper 테스트")
// class WaterMapperTest {
//
//  private WaterMapper waterMapper;
//
//  @BeforeEach
//  void setUp() {
//    waterMapper = new WaterMapper();
//  }
//
//  @Nested
//  @DisplayName("map() 메서드 테스트")
//  class MapMethod {
//
//    @Test
//    @DisplayName("null이 입력되면 null을 반환한다")
//    void shouldReturnNullWhenInputIsNull() {
//      // given
//      List<WaterEvaluation> waterEvaluations = null;
//
//      // when
//      WaterReport result = waterMapper.map(waterEvaluations);
//
//      // then
//      assertThat(result).isNull();
//    }
//
//    @Test
//    @DisplayName("빈 리스트가 입력되면 null을 반환한다")
//    void shouldReturnNullWhenInputIsEmpty() {
//      // given
//      List<WaterEvaluation> waterEvaluations = Collections.emptyList();
//
//      // when
//      WaterReport result = waterMapper.map(waterEvaluations);
//
//      // then
//      assertThat(result).isNull();
//    }
//
//    @Nested
//    @DisplayName("오늘과 어제 데이터가 모두 있는 경우")
//    class WhenBothTodayAndYesterdayExist {
//
//      @Test
//      @DisplayName("HIGH 레벨일 때 성공 메시지와 적절한 데이터를 반환한다")
//      void shouldReturnSuccessMessageForHighLevel() {
//        // given
//        WaterEvaluation today = new WaterEvaluation(10, WaterLevel.HIGH, DayType.TODAY);
//        WaterEvaluation yesterday = new WaterEvaluation(8, WaterLevel.MEDIUM, DayType.YESTERDAY);
//        List<WaterEvaluation> waterEvaluations = List.of(today, yesterday);
//
//        // when
//        WaterReport result = waterMapper.map(waterEvaluations);
//
//        // then
//        assertThat(result).isNotNull();
//        assertThat(result.message()).isEqualTo("훌륭해요! 물 섭취 만점입니다. 앞으로도 잘 유지해봐요");
//        assertThat(result.items()).hasSize(3);
//
//        // STANDARD 항목 검증
//        WaterReportItem standardItem = result.items().getFirst();
//        assertThat(standardItem.name()).isEqualTo("STANDARD");
//        assertThat(standardItem.value()).isEqualTo(2000.0);
//        assertThat(standardItem.color()).isEqualTo(COLOR_MAP.get(WaterSuggestion.NONE));
//        assertThat(standardItem.level()).isEqualTo(WaterSuggestion.NONE);
//
//        // YESTERDAY 항목 검증
//        WaterReportItem yesterdayItem = result.items().get(1);
//        assertThat(yesterdayItem.name()).isEqualTo("YESTERDAY");
//        assertThat(yesterdayItem.value()).isEqualTo(1600.0); // 8 * 200
//        assertThat(yesterdayItem.color()).isEqualTo(COLOR_MAP.get(WaterSuggestion.MEDIUM));
//        assertThat(yesterdayItem.level()).isEqualTo(WaterSuggestion.MEDIUM);
//
//        // TODAY 항목 검증
//        WaterReportItem todayItem = result.items().get(2);
//        assertThat(todayItem.name()).isEqualTo("TODAY");
//        assertThat(todayItem.value()).isEqualTo(2000.0); // 10 * 200
//        assertThat(todayItem.color()).isEqualTo(COLOR_MAP.get(WaterSuggestion.HIGH));
//        assertThat(todayItem.level()).isEqualTo(WaterSuggestion.HIGH);
//      }
//
//      @Test
//      @DisplayName("MEDIUM 레벨일 때 보통 메시지를 반환한다")
//      void shouldReturnMediumMessageForMediumLevel() {
//        // given
//        WaterEvaluation today = new WaterEvaluation(7, WaterLevel.MEDIUM, DayType.TODAY);
//        WaterEvaluation yesterday = new WaterEvaluation(6, WaterLevel.LOW, DayType.YESTERDAY);
//        List<WaterEvaluation> waterEvaluations = List.of(today, yesterday);
//
//        // when
//        WaterReport result = waterMapper.map(waterEvaluations);
//
//        // then
//        assertThat(result).isNotNull();
//        assertThat(result.message()).isEqualTo("보통 수준이에요. 조금 더 자주 물을 마셔보세요!");
//        assertThat(result.items().get(2).value()).isEqualTo(1400.0); // 7 * 200
//        assertThat(result.items().get(2).level()).isEqualTo(WaterSuggestion.MEDIUM);
//      }
//
//      @Test
//      @DisplayName("LOW 레벨일 때 경고 메시지를 반환한다")
//      void shouldReturnWarningMessageForLowLevel() {
//        // given
//        WaterEvaluation today = new WaterEvaluation(3, WaterLevel.LOW, DayType.TODAY);
//        WaterEvaluation yesterday = new WaterEvaluation(5, WaterLevel.MEDIUM, DayType.YESTERDAY);
//        List<WaterEvaluation> waterEvaluations = List.of(today, yesterday);
//
//        // when
//        WaterReport result = waterMapper.map(waterEvaluations);
//
//        // then
//        assertThat(result).isNotNull();
//        assertThat(result.message()).isEqualTo("장이 말라가고 있어요! 물 섭취량을 늘려야 해요");
//        assertThat(result.items().get(2).value()).isEqualTo(600.0); // 3 * 200
//        assertThat(result.items().get(2).level()).isEqualTo(WaterSuggestion.LOW);
//      }
//
//      @Test
//      @DisplayName("NONE 레벨일 때 기록 없음 메시지를 반환한다")
//      void shouldReturnNoRecordMessageForNoneLevel() {
//        // given
//        WaterEvaluation today = new WaterEvaluation(0, WaterLevel.NONE, DayType.TODAY);
//        WaterEvaluation yesterday = new WaterEvaluation(5, WaterLevel.MEDIUM, DayType.YESTERDAY);
//        List<WaterEvaluation> waterEvaluations = List.of(today, yesterday);
//
//        // when
//        WaterReport result = waterMapper.map(waterEvaluations);
//
//        // then
//        assertThat(result).isNotNull();
//        assertThat(result.message()).isEqualTo("물 섭취 기록이 없어요. 물을 자주 마셔주세요");
//        assertThat(result.items().get(2).value()).isEqualTo(0.0);
//        assertThat(result.items().get(2).level()).isEqualTo(WaterSuggestion.NONE);
//      }
//    }
//
//    @Nested
//    @DisplayName("오늘 데이터만 있는 경우")
//    class WhenOnlyTodayExists {
//
//      @Test
//      @DisplayName("어제 데이터가 없으면 어제 항목은 0으로 처리된다")
//      void shouldHandleMissingYesterdayData() {
//        // given
//        WaterEvaluation today = new WaterEvaluation(8, WaterLevel.MEDIUM, DayType.TODAY);
//        List<WaterEvaluation> waterEvaluations = List.of(today);
//
//        // when
//        WaterReport result = waterMapper.map(waterEvaluations);
//
//        // then
//        assertThat(result).isNotNull();
//        assertThat(result.items()).hasSize(3);
//
//        // YESTERDAY 항목 검증 - null evaluation이므로 0.0
//        WaterReportItem yesterdayItem = result.items().get(1);
//        assertThat(yesterdayItem.name()).isEqualTo("YESTERDAY");
//        assertThat(yesterdayItem.value()).isEqualTo(0.0);
//        assertThat(yesterdayItem.color()).isEqualTo(COLOR_MAP.get(WaterSuggestion.NONE));
//        assertThat(yesterdayItem.level()).isEqualTo(WaterSuggestion.NONE);
//
//        // TODAY 항목 검증
//        WaterReportItem todayItem = result.items().get(2);
//        assertThat(todayItem.name()).isEqualTo("TODAY");
//        assertThat(todayItem.value()).isEqualTo(1600.0); // 8 * 200
//      }
//    }
//
//    @Nested
//    @DisplayName("어제 데이터만 있는 경우")
//    class WhenOnlyYesterdayExists {
//
//      @Test
//      @DisplayName("오늘 데이터가 없으면 기본 메시지를 반환하고 오늘 항목은 0으로 처리된다")
//      void shouldHandleMissingTodayData() {
//        // given
//        WaterEvaluation yesterday = new WaterEvaluation(9, WaterLevel.HIGH, DayType.YESTERDAY);
//        List<WaterEvaluation> waterEvaluations = List.of(yesterday);
//
//        // when
//        WaterReport result = waterMapper.map(waterEvaluations);
//
//        // then
//        assertThat(result).isNotNull();
//        assertThat(result.message()).isEqualTo("물 섭취 기록이 없어요. 물을 자주 마셔주세요");
//        assertThat(result.items()).hasSize(3);
//
//        // YESTERDAY 항목 검증
//        WaterReportItem yesterdayItem = result.items().get(1);
//        assertThat(yesterdayItem.name()).isEqualTo("YESTERDAY");
//        assertThat(yesterdayItem.value()).isEqualTo(1800.0); // 9 * 200
//
//        // TODAY 항목 검증 - null evaluation이므로 0.0
//        WaterReportItem todayItem = result.items().get(2);
//        assertThat(todayItem.name()).isEqualTo("TODAY");
//        assertThat(todayItem.value()).isEqualTo(0.0);
//        assertThat(todayItem.color()).isEqualTo(COLOR_MAP.get(WaterSuggestion.NONE));
//        assertThat(todayItem.level()).isEqualTo(WaterSuggestion.NONE);
//      }
//    }
//
//    @Test
//    @DisplayName("quantity가 0이면 value도 0이 된다")
//    void shouldHandleZeroQuantity() {
//      // given
//      WaterEvaluation today = new WaterEvaluation(0, WaterLevel.NONE, DayType.TODAY);
//      WaterEvaluation yesterday = new WaterEvaluation(0, WaterLevel.NONE, DayType.YESTERDAY);
//      List<WaterEvaluation> waterEvaluations = List.of(today, yesterday);
//
//      // when
//      WaterReport result = waterMapper.map(waterEvaluations);
//
//      // then
//      assertThat(result).isNotNull();
//      assertThat(result.items().get(1).value()).isEqualTo(0.0); // YESTERDAY
//      assertThat(result.items().get(2).value()).isEqualTo(0.0); // TODAY
//    }
//  }
//
//  @Nested
//  @DisplayName("WaterSuggestion과 WaterLevel 매핑 테스트")
//  class WaterSuggestionMapping {
//
//    @Test
//    @DisplayName("HIGH 레벨은 HIGH suggestion과 파란색으로 매핑된다")
//    void shouldMapHighLevelCorrectly() {
//      // given
//      WaterEvaluation evaluation = new WaterEvaluation(10, WaterLevel.HIGH, DayType.TODAY);
//
//      // when
//      WaterReport result = waterMapper.map(List.of(evaluation));
//
//      // then
//      WaterReportItem todayItem = result.items().get(2);
//      assertThat(todayItem.level()).isEqualTo(WaterSuggestion.HIGH);
//      assertThat(todayItem.color()).isEqualTo("#23ABFF");
//    }
//
//    @Test
//    @DisplayName("MEDIUM 레벨은 MEDIUM suggestion과 주황색으로 매핑된다")
//    void shouldMapMediumLevelCorrectly() {
//      // given
//      WaterEvaluation evaluation = new WaterEvaluation(7, WaterLevel.MEDIUM, DayType.TODAY);
//
//      // when
//      WaterReport result = waterMapper.map(List.of(evaluation));
//
//      // then
//      WaterReportItem todayItem = result.items().get(2);
//      assertThat(todayItem.level()).isEqualTo(WaterSuggestion.MEDIUM);
//      assertThat(todayItem.color()).isEqualTo("#F4B005");
//    }
//
//    @Test
//    @DisplayName("LOW 레벨은 LOW suggestion과 빨간색으로 매핑된다")
//    void shouldMapLowLevelCorrectly() {
//      // given
//      WaterEvaluation evaluation = new WaterEvaluation(3, WaterLevel.LOW, DayType.TODAY);
//
//      // when
//      WaterReport result = waterMapper.map(List.of(evaluation));
//
//      // then
//      WaterReportItem todayItem = result.items().get(2);
//      assertThat(todayItem.level()).isEqualTo(WaterSuggestion.LOW);
//      assertThat(todayItem.color()).isEqualTo("#F13A49");
//    }
//
//    @Test
//    @DisplayName("NONE 레벨은 NONE suggestion과 회색으로 매핑된다")
//    void shouldMapNoneLevelCorrectly() {
//      // given
//      WaterEvaluation evaluation = new WaterEvaluation(0, WaterLevel.NONE, DayType.TODAY);
//
//      // when
//      WaterReport result = waterMapper.map(List.of(evaluation));
//
//      // then
//      WaterReportItem todayItem = result.items().get(2);
//      assertThat(todayItem.level()).isEqualTo(WaterSuggestion.NONE);
//      assertThat(todayItem.color()).isEqualTo("#D9D9D9");
//    }
//  }
//
//  @Nested
//  @DisplayName("STANDARD 항목 테스트")
//  class StandardItemTest {
//
//    @Test
//    @DisplayName("STANDARD 항목은 항상 2000.0 값과 NONE suggestion을 가진다")
//    void shouldAlwaysHaveStandardItemWithFixedValues() {
//      // given
//      WaterEvaluation today = new WaterEvaluation(10, WaterLevel.HIGH, DayType.TODAY);
//      List<WaterEvaluation> waterEvaluations = List.of(today);
//
//      // when
//      WaterReport result = waterMapper.map(waterEvaluations);
//
//      // then
//      WaterReportItem standardItem = result.items().getFirst();
//      assertThat(standardItem.name()).isEqualTo("STANDARD");
//      assertThat(standardItem.value()).isEqualTo(2000.0);
//      assertThat(standardItem.color()).isEqualTo(COLOR_MAP.get(WaterSuggestion.NONE));
//      assertThat(standardItem.level()).isEqualTo(WaterSuggestion.NONE);
//    }
//  }
//
//  @Nested
//  @DisplayName("메시지 생성 테스트")
//  class MessageGenerationTest {
//
//    @Test
//    @DisplayName("모든 WaterLevel에 대해 적절한 메시지가 생성된다")
//    void shouldGenerateAppropriateMessagesForAllLevels() {
//      // HIGH
//      WaterEvaluation high = new WaterEvaluation(10, WaterLevel.HIGH, DayType.TODAY);
//      WaterReport highResult = waterMapper.map(List.of(high));
//      assertThat(highResult.message()).isEqualTo("훌륭해요! 물 섭취 만점입니다. 앞으로도 잘 유지해봐요");
//
//      // MEDIUM
//      WaterEvaluation medium = new WaterEvaluation(7, WaterLevel.MEDIUM, DayType.TODAY);
//      WaterReport mediumResult = waterMapper.map(List.of(medium));
//      assertThat(mediumResult.message()).isEqualTo("보통 수준이에요. 조금 더 자주 물을 마셔보세요!");
//
//      // LOW
//      WaterEvaluation low = new WaterEvaluation(3, WaterLevel.LOW, DayType.TODAY);
//      WaterReport lowResult = waterMapper.map(List.of(low));
//      assertThat(lowResult.message()).isEqualTo("장이 말라가고 있어요! 물 섭취량을 늘려야 해요");
//
//      // NONE
//      WaterEvaluation none = new WaterEvaluation(0, WaterLevel.NONE, DayType.TODAY);
//      WaterReport noneResult = waterMapper.map(List.of(none));
//      assertThat(noneResult.message()).isEqualTo("물 섭취 기록이 없어요. 물을 자주 마셔주세요");
//    }
//  }
//
//  @Nested
//  @DisplayName("수량 계산 테스트")
//  class QuantityCalculationTest {
//
//    @Test
//    @DisplayName("quantity는 200을 곱해서 value로 변환된다")
//    void shouldMultiplyQuantityBy200() {
//      // given
//      WaterEvaluation today = new WaterEvaluation(5, WaterLevel.MEDIUM, DayType.TODAY);
//      WaterEvaluation yesterday = new WaterEvaluation(3, WaterLevel.LOW, DayType.YESTERDAY);
//      List<WaterEvaluation> waterEvaluations = List.of(today, yesterday);
//
//      // when
//      WaterReport result = waterMapper.map(waterEvaluations);
//
//      // then
//      assertThat(result.items().get(1).value()).isEqualTo(600.0); // 3 * 200
//      assertThat(result.items().get(2).value()).isEqualTo(1000.0); // 5 * 200
//    }
//
//    @Test
//    @DisplayName("큰 수량도 올바르게 계산된다")
//    void shouldHandleLargeQuantities() {
//      // given
//      WaterEvaluation today = new WaterEvaluation(15, WaterLevel.HIGH, DayType.TODAY);
//      List<WaterEvaluation> waterEvaluations = List.of(today);
//
//      // when
//      WaterReport result = waterMapper.map(waterEvaluations);
//
//      // then
//      assertThat(result.items().get(2).value()).isEqualTo(3000.0); // 15 * 200
//    }
//
//    @Test
//    @DisplayName("quantity가 null인 경우 0으로 처리한다")
//    void shouldHandleZeroQuantity() {
//      // given
//      WaterEvaluation today = new WaterEvaluation(null, WaterLevel.NONE, DayType.TODAY);
//      WaterEvaluation yesterday = new WaterEvaluation(null, WaterLevel.NONE, DayType.YESTERDAY);
//      List<WaterEvaluation> waterEvaluations = List.of(today, yesterday);
//
//      // when
//      WaterReport result = waterMapper.map(waterEvaluations);
//
//      // then
//      assertThat(result).isNotNull();
//      assertThat(result.items().get(1).value()).isEqualTo(0.0);
//      assertThat(result.items().get(2).value()).isEqualTo(0.0);
//    }
//  }
// }
