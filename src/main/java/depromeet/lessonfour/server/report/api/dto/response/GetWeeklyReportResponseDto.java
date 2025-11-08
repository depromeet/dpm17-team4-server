package depromeet.lessonfour.server.report.api.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record GetWeeklyReportResponseDto(
    LocalDateTime updatedAt,
    DefecationScore defecationScore,
    UserAverage userAverage,
    FoodSection food,
    WaterSection water,
    StressSection stress,
    SuggestionSection suggestion,
    String externalLink) {

  public record DefecationScore(double lastWeek, double thisWeek, List<Double> dailyScore) {}

  public record UserAverage(double me, double average, double topPercent) {}

  public record FoodSection(
      String message, WeeklyComparison weeklyComparison, List<FoodItem> items) {
    public record WeeklyComparison(int lastWeek, int thisWeek) {}

    public record FoodItem(String occurredAt, String mealTime, List<String> foods) {}
  }

  public record WaterSection(String message, List<WaterItem> items) {
    public record WaterItem(String name, double value) {}
  }

  public record StressSection(String message, String image, List<StressItem> items) {
    public record StressItem(String day, String stress) {}
  }

  public record SuggestionSection(String message, List<SuggestionItem> items) {
    public record SuggestionItem(String image, String title, String content) {}
  }
}
