package depromeet.lessonfour.server.report.api.dto.response;

import java.util.List;

import depromeet.lessonfour.server.report.app.dto.response.ToiletColorCount;
import depromeet.lessonfour.server.report.domain.vo.monthly.ToiletTimeDistribution;

public record GetMonthlyReportResponseDto(
    MonthlyRecordCount recordCount,
    List<MonthlyToiletShape> shape,
    MonthlyToiletTime timeDistribution,
    MonthlyToiletColor color,
    MonthlyToiletPain pain,
    List<MonthlyToiletPeriod> timeOfDay) {

  public record MonthlyRecordCount(
      int totalRecordCounts, int defecationRecordCounts, int lifestyleRecordCounts) {}

  public record MonthlyToiletShape(String shape, int count, String message) {}

  public record MonthlyToiletTime(int within5min, int over5min, int over10min) {

    public static MonthlyToiletTime from(ToiletTimeDistribution toiletTimeDistribution) {
      return new MonthlyToiletTime(
          toiletTimeDistribution.within5min(),
          toiletTimeDistribution.over5min(),
          toiletTimeDistribution.over10min());
    }
  }

  public record MonthlyToiletColor(List<ColorCount> items, String colorMessage) {

    public record ColorCount(String color, int count) {

      public static ColorCount from(ToiletColorCount toiletColorCount) {
        return new ColorCount(toiletColorCount.color().getValue(), toiletColorCount.count());
      }
    }
  }

  public record MonthlyToiletPain(
      int veryLow, int low, int medium, int high, int veryHigh, ToiletPainComparison comparison) {

    public record ToiletPainComparison(String direction, int count) {}
  }

  public record MonthlyToiletPeriod(String period, int count) {}
}
