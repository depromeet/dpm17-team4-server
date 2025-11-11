package depromeet.lessonfour.server.report.api.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import depromeet.lessonfour.server.activityrecord.domain.vo.MealTime;
import depromeet.lessonfour.server.report.domain.vo.activity.WaterLevel;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletColor;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletShape;

public record GetDailyReportResponseDto(
    LocalDateTime updatedAt,
    DailyToiletReportDto poo,
    DailyFoodReport food,
    DailyWaterReport water,
    DailyStressReport stress) {

  // POO
  public record DailyToiletReportDto(
      double score, ToiletSummary summary, List<ToiletReportItem> items) {}

  public record ToiletSummary(
      String image, List<String> backgroundColors, String caption, String message) {}

  public record ToiletReportItem(
      LocalDateTime occurredAt,
      String message,
      ToiletColor color,
      ToiletShape shape,
      int duration,
      double pain,
      String note) {}

  // FOOD
  public record DailyFoodReport(String message, List<FoodReportItem> items) {}

  public record FoodReportItem(LocalDate occurredAt, List<DailyFoodReportMeal> meals) {}

  public record DailyFoodReportMeal(MealTime mealTime, boolean dangerous, List<String> foods) {}

  // WATER
  public record DailyWaterReport(String message, List<WaterReportItem> items) {}

  public record WaterReportItem(String name, double value, String color, WaterLevel level) {}

  // STRESS
  public record DailyStressReport(String message, String image) {}
}
