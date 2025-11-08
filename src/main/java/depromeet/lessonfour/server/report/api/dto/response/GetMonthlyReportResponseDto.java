package depromeet.lessonfour.server.report.api.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import depromeet.lessonfour.server.report.app.dto.response.MonthlyScore;
import depromeet.lessonfour.server.report.app.dto.response.ToiletColorCount;
import depromeet.lessonfour.server.report.domain.vo.monthly.ToiletTimeDistribution;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletColor;

public record GetMonthlyReportResponseDto(
    MonthlyRecordCount monthlyRecordCounts,
    List<Integer> monthlyDefecationScore,
    UserAverage userAverage,
    MonthlyScore monthlyScore,
    ToiletShapeSection shape,
    MonthlyToiletTime timeDistribution,
    MonthlyToiletColor color,
    MonthlyToiletPain pain,
    TimeOfDaySection timeOfDay,
    FoodSection food,
    WaterSection water,
    StressSection stress,
    SuggestionSection suggestion) {

  // ====== 공통 상단 ======
  public record MonthlyRecordCount(
      int totalRecordCounts, int defecationRecordCounts, int lifestyleRecordCounts) {}

  public record UserAverage(double me, double average, double topPercent, String titleMessage) {}

  // ====== shape ======
  public record ToiletShapeSection(String titleMessage, List<MonthlyToiletShape> items) {}

  public record MonthlyToiletShape(
      String shape, int count, @JsonProperty("warning") String message) {}

  // ====== timeDistribution ======
  public record MonthlyToiletTime(
      int within5min, int over5min, int over10min, String titleMessage) {
    public static MonthlyToiletTime from(
        ToiletTimeDistribution toiletTimeDistribution, String titleMessage) {
      return new MonthlyToiletTime(
          toiletTimeDistribution.within5min(),
          toiletTimeDistribution.over5min(),
          toiletTimeDistribution.over10min(),
          titleMessage);
    }
  }

  // ====== color ======
  public record MonthlyToiletColor(
      List<ColorCount> items, String colorMessage, String titleMessage) {

    public record ColorCount(String color, int count, String warning) {

      public static ColorCount from(ToiletColorCount toiletColorCount) {
        return new ColorCount(
            toiletColorCount.color().getValue(),
            toiletColorCount.count(),
            toiletColorCount.color().equals(ToiletColor.RED) ? "전문가 상담 권장" : null);
      }
    }
  }

  // ====== pain ======
  public record MonthlyToiletPain(
      String titleMessage,
      int veryLow,
      int low,
      int medium,
      int high,
      int veryHigh,
      ToiletPainComparison comparison) {

    public record ToiletPainComparison(String direction, int count) {}
  }

  // ====== timeOfDay ======
  public record TimeOfDaySection(String titleMessage, List<MonthlyToiletPeriod> items) {}

  public record MonthlyToiletPeriod(String period, int count) {}

  // ====== food ======
  public record FoodSection(
      String message, MonthlyComparison monthlyComparison, List<WeeklyGroup> weeklyGroups) {
    public record MonthlyComparison(int lastMonth, int thisMonth) {}

    public record WeeklyGroup(
        String weekLabel, String startDate, String endDate, List<FoodItem> items) {
      public record FoodItem(String occurredAt, String mealTime, List<String> foods) {}
    }
  }

  // ====== water ======
  public record WaterSection(String message, List<WaterItem> items) {
    public record WaterItem(String name, double value) {}
  }

  // ====== stress ======
  public record StressSection(String message, String image, List<StressItem> items) {
    public record StressItem(String day, String stress) {}
  }

  // ====== suggestion ======
  public record SuggestionSection(String message, List<SuggestionItem> items) {
    public record SuggestionItem(String image, String title, String content) {}
  }
}
