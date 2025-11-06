package depromeet.lessonfour.server.report.api.dto.response;

import java.util.List;

public record GetMonthlyReportResponse(
    MonthlyRecordCount recordCount,
    List<MonthlyToiletShape> shape,
    MonthlyToiletTime timeDistribution,
    MonthlyToiletColor color,
    MonthlyToiletPain pain,
    List<MonthlyToiletPeriod> timeOfDay) {

  record MonthlyRecordCount(
      int totalRecordCounts, int defecationRecordCounts, int lifestyleRecordCounts) {}

  record MonthlyToiletShape(String shape, int count, String message) {}

  record MonthlyToiletTime(int within5min, int over5min, int over10min) {}

  record MonthlyToiletColor(List<ToiletColor> items, String colorMessage) {
    record ToiletColor(String color, int count) {}
  }

  record MonthlyToiletPain(
      int veryLow, int low, int medium, int high, int veryHigh, ToiletPainComparison comparison) {
    record ToiletPainComparison(String direction, int count) {}
  }

  record MonthlyToiletPeriod(String period, int count) {}
}
