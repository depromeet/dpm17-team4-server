package depromeet.lessonfour.server.report.api.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import depromeet.lessonfour.server.report.domain.vo.MonthlyScore;
import depromeet.lessonfour.server.report.domain.vo.toilet.ToiletColorCount;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletColor;

public record GetMonthlyReportResponseDto(
    MonthlyRecordCount monthlyRecordCounts,
    List<Integer> monthlyDefecationScore,
    UserAverage userAverage,
    MonthlyScore monthlyScore,
    MonthlyShapeSection shape,
    MonthlyTimeDistributionSection timeDistribution,
    MonthlyColorSection color,
    MonthlyPainSection pain,
    MonthlyPeriodSection timeOfDay,
    MonthlyFoodSection food,
    MonthlyWaterSection water,
    MonthlyStressSection stress,
    SuggestionSection suggestion) {

  // ====== 공통 상단 ======
  public record MonthlyRecordCount(
      int totalRecordCounts, int defecationRecordCounts, int lifestyleRecordCounts) {}

  public record UserAverage(double me, double average, double topPercent, String titleMessage) {}

  // ====== shape ======
  public record MonthlyShapeSection(String titleMessage, List<MonthlyToiletShape> items) {}

  public record MonthlyToiletShape(
      String shape, int count, @JsonProperty("warning") String message) {}

  // ====== timeDistribution ======
  public record MonthlyTimeDistributionSection(
      String titleMessage, int within5min, int over5min, int over10min, String warning) {}

  // ====== color ======
  public record MonthlyColorSection(
      String titleMessage, String colorMessage, List<ColorCount> items) {}

  public record ColorCount(String color, int count, String warning) {

    public static ColorCount from(ToiletColorCount toiletColorCount) {
      return new ColorCount(
          toiletColorCount.color().getValue(),
          toiletColorCount.count(),
          toiletColorCount.color().equals(ToiletColor.RED) ? "전문가 상담 권장" : null);
    }
  }

  // ====== pain ======
  public record MonthlyPainSection(
      String titleMessage,
      int veryLow,
      int low,
      int medium,
      int high,
      int veryHigh,
      ToiletPainComparison comparison) {

    public record ToiletPainComparison(String direction, int count) {
      public static ToiletPainComparison from(int painDiff) {
        if (painDiff > 0) {
          return new ToiletPainComparison("increased", painDiff);
        } else if (painDiff < 0) {
          return new ToiletPainComparison("decreased", -painDiff);
        } else {
          return new ToiletPainComparison("same", 0);
        }
      }
    }
  }

  // ====== timeOfDay ======
  public record MonthlyPeriodSection(String titleMessage, List<MonthlyToiletPeriod> items) {
    public record MonthlyToiletPeriod(String period, int count) {}
  }

  // ====== food ======
  public record MonthlyFoodSection(
      String message, MonthlyComparison monthlyComparison, List<WeeklyFoodGroup> weeklyGroups) {
    public static MonthlyFoodSection empty() {
      return new MonthlyFoodSection(
          "음식 기록이 없어요. 장에 좋은 음식을 먹어볼까요?", new MonthlyComparison(0, 0), List.of());
    }

    public record MonthlyComparison(int lastMonth, int thisMonth) {}

    public record WeeklyFoodGroup(
        String weekLabel, String startDate, String endDate, List<FoodItem> items) {
      public record FoodItem(String occurredAt, String mealTime, List<String> foods) {}
    }
  }

  // ====== water ======
  public record MonthlyWaterSection(String message, List<WaterItem> items) {
    public static MonthlyWaterSection empty() {
      return new MonthlyWaterSection("물 섭취 기록이 없어요\n물을 자주 마셔주세요", List.of());
    }

    public record WaterItem(String name, double value) {}
  }

  // ====== stress ======
  public record MonthlyStressSection(String message, String image, List<StressItem> items) {
    public static MonthlyStressSection empty() {
      return new MonthlyStressSection("스트레스 기록이 비어있어요\n" + "기록을 시작해보세요", "", List.of());
    }

    public record StressItem(String day, String stress) {}
  }
}
