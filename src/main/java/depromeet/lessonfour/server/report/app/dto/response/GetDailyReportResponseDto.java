package depromeet.lessonfour.server.report.app.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import depromeet.lessonfour.server.activityrecord.domain.vo.MealTime;
import depromeet.lessonfour.server.report.domain.vo.Suggestion.WaterSuggestion;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletColor;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletShape;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record GetDailyReportResponseDto(
    LocalDateTime updatedAt,
    StoolDailyReport poo,
    FoodDailyReport food,
    WaterReport water,
    StressReport stress,
    SuggestionDto suggestion) {

  public static GetDailyReportResponseDto from() {
    return GetDailyReportResponseDto.builder().build();
  }

  // POO
  @Builder(access = AccessLevel.PRIVATE)
  public record StoolDailyReport(double score, StoolSummary summary, List<StoolReportItem> items) {}

  public record StoolSummary(
      String image, List<String> backgroundColors, String caption, String message) {}

  public record StoolReportItem(
      LocalDateTime occurredAt,
      String message,
      ToiletColor color,
      ToiletShape shape,
      int duration,
      double pain,
      String note) {}

  // FOOD
  public record FoodDailyReport(String message, List<FoodReportItem> items) {}

  public record FoodReportItem(LocalDateTime occurredAt, List<FoodReportMeal> meals) {}

  public record FoodReportMeal(MealTime mealTime, boolean dangerous, List<String> foods) {}

  // WATER
  public record WaterReport(String message, List<WaterReportItem> items) {}

  public record WaterReportItem(String name, double value, String color, WaterSuggestion level) {}

  // STRESS
  public record StressReport(String message, String image) {}

  // SUGGESTION
  public record SuggestionDto(String message, List<SuggestionItem> items) {}

  public record SuggestionItem(String image, String title, String content) {}
}
