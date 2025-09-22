package depromeet.lessonfour.server.report.app.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.activityrecord.domain.vo.MealTime;
import depromeet.lessonfour.server.report.app.dto.response.GetDailyReportResponseDto;
import depromeet.lessonfour.server.report.app.dto.response.GetDailyReportResponseDto.FoodDailyReport;
import depromeet.lessonfour.server.report.app.dto.response.GetDailyReportResponseDto.FoodReportItem;
import depromeet.lessonfour.server.report.app.dto.response.GetDailyReportResponseDto.FoodReportMeal;
import depromeet.lessonfour.server.report.app.dto.response.GetDailyReportResponseDto.PooDailyReport;
import depromeet.lessonfour.server.report.app.dto.response.GetDailyReportResponseDto.PooReportItem;
import depromeet.lessonfour.server.report.app.dto.response.GetDailyReportResponseDto.PooSummary;
import depromeet.lessonfour.server.report.app.dto.response.GetDailyReportResponseDto.StressReport;
import depromeet.lessonfour.server.report.app.dto.response.GetDailyReportResponseDto.Suggestion;
import depromeet.lessonfour.server.report.app.dto.response.GetDailyReportResponseDto.SuggestionItem;
import depromeet.lessonfour.server.report.app.dto.response.GetDailyReportResponseDto.WaterReport;
import depromeet.lessonfour.server.report.app.dto.response.GetDailyReportResponseDto.WaterReportItem;
import depromeet.lessonfour.server.report.domain.vo.FoodEvaluation;
import depromeet.lessonfour.server.report.domain.vo.PooEvaluation;
import depromeet.lessonfour.server.report.domain.vo.StressEvaluation;
import depromeet.lessonfour.server.report.domain.vo.SuggestionEvaluation;
import depromeet.lessonfour.server.report.domain.vo.WaterEvaluation;
import depromeet.lessonfour.server.report.domain.vo.WaterLevel;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletColor;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletShape;

@Component
public class DailyReportMapper {

  public GetDailyReportResponseDto toResponse(
      LocalDateTime updatedAt,
      FoodEvaluation food,
      PooEvaluation poo,
      StressEvaluation stress,
      WaterEvaluation water,
      SuggestionEvaluation suggestion) {
    return new GetDailyReportResponseDto(
        updatedAt,
        mapPooDto(poo),
        mapFoodDto(food),
        mapWaterDto(water),
        mapStressDto(stress),
        mapSuggestionDto(suggestion));
  }

  private PooDailyReport mapPooDto(PooEvaluation poo) {
    return new GetDailyReportResponseDto.PooDailyReport(
        85.5,
        new PooSummary(
            "http://dummy_image.png", List.of("#A4141E", "#FF535F"), "화가 잔뜩 난 대장", "전문가 상담이 필요해요"),
        List.of(
            new PooReportItem(
                LocalDateTime.now(),
                "전문가의 상담이 필요해요. 복통이\n" + "매우 심했다면 단순한 식사 문제를\n" + "넘어서 장염이나 자극적인 음식 섭취",
                ToiletColor.DARK_BROWN,
                ToiletShape.PORRIDGE,
                10,
                78.0,
                "배가 너무 아팠어요")));
  }

  private FoodDailyReport mapFoodDto(FoodEvaluation food) {
    return new GetDailyReportResponseDto.FoodDailyReport(
        "맵고 자극적인 음식이 장을 자극했을 수 있어요",
        List.of(
            new FoodReportItem(
                LocalDateTime.now().minusDays(1),
                List.of(
                    new FoodReportMeal(MealTime.BREAKFAST, false, List.of("토스트", "커피", "계란")),
                    new FoodReportMeal(MealTime.LUNCH, true, List.of("떡볶이", "순대", "튀김")),
                    new FoodReportMeal(MealTime.DINNER, false, List.of("샐러드", "닭가슴살", "고구마")),
                    new FoodReportMeal(MealTime.SNACK, false, List.of("아이스크림", "과자")))),
            new FoodReportItem(
                LocalDateTime.now(),
                List.of(
                    new FoodReportMeal(MealTime.BREAKFAST, false, List.of("우유", "바나나", "시리얼")),
                    new FoodReportMeal(MealTime.LUNCH, false, List.of("된장찌개", "고등어구이", "김치")),
                    new FoodReportMeal(MealTime.DINNER, false, List.of("닭가슴살", "샐러드", "고구마")),
                    new FoodReportMeal(MealTime.SNACK, false, List.of("견과류"))))));
  }

  private WaterReport mapWaterDto(WaterEvaluation water) {
    return new WaterReport(
        "장이 말라가고 있어요! 물 섭취량을 늘려야 해요",
        List.of(
            new WaterReportItem(
                "STANDARD", 2000.0, WaterLevel.STANDARD.getColor(), WaterLevel.STANDARD),
            new WaterReportItem(
                "YESTERDAY", 500.0, WaterLevel.MEDIUM.getColor(), WaterLevel.MEDIUM),
            new WaterReportItem("TODAY", 300.0, WaterLevel.LOW.getColor(), WaterLevel.LOW)));
  }

  private StressReport mapStressDto(StressEvaluation stress) {
    return new StressReport("스트레스 관리가 필요해요.\n가벼운 산책이나 명상은 어때요?", "http://dummy_stress_image.png");
  }

  private Suggestion mapSuggestionDto(SuggestionEvaluation suggestion) {
    return new Suggestion(
        "장 상태를 개선하려면 이런 습관을 추천해요",
        List.of(
            new SuggestionItem("http://dummy-1.png", "물 섭취량을 더 늘려보세요", "하루 권장 물 섭취량은 성인 기준 2L 예요"),
            new SuggestionItem("http://dummy-2.png", "충분한 식이섬유가 중요해요", "과일과 채소를 섭취하면 좋은 흐름이 유지돼요"),
            new SuggestionItem(
                "http://dummy-3.png", "지속적으로 배변을 기록해요", "배변이 잘 되는 나만의 루틴을 만들 수 있어요")));
  }
}
