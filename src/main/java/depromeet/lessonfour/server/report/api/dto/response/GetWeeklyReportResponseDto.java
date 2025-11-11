package depromeet.lessonfour.server.report.api.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record GetWeeklyReportResponseDto(
    LocalDateTime updatedAt,
    DefecationScore defecationScore,
    UserAverage userAverage,
    WeeklyFoodSection food,
    WeeklyWaterSection water,
    WeeklyStressSection stress,
    SuggestionSection suggestion) {

  public record DefecationScore(double lastWeek, double thisWeek, List<Double> dailyScore) {}

  public record UserAverage(double me, double average, double topPercent) {}

  public record WeeklyFoodSection(
      String message, WeeklyComparison weeklyComparison, List<FoodItem> items) {
    public record WeeklyComparison(int lastWeek, int thisWeek) {}

    public record FoodItem(String occurredAt, String mealTime, List<String> foods) {}
  }

  public record WeeklyWaterSection(String message, List<WaterItem> items) {
    public record WaterItem(String name, double value) {}
  }

  public record WeeklyStressSection(String message, String image, List<StressItem> items) {
    public record StressItem(String day, String stress) {}
  }
}
