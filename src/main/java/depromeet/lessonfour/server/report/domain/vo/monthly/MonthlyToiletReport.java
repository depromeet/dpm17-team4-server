package depromeet.lessonfour.server.report.domain.vo.monthly;

import java.util.List;

import depromeet.lessonfour.server.report.app.dto.response.ToiletColorCount;
import depromeet.lessonfour.server.report.app.dto.response.ToiletShapeCount;

public record MonthlyToiletReport(
    int size,
    ScoreSummary scoreSummary,
    List<ToiletShapeCount> shapeCount,
    ToiletTimeDistribution timeDistribution,
    List<ToiletColorCount> colorCount,
    ToiletPainDistribution painDistribution,
    List<ToiletPeriodCount> periodCount) {

  public static MonthlyToiletReport of(
      int size,
      ScoreSummary scoreSummary,
      List<ToiletShapeCount> shapeCount,
      ToiletTimeDistribution timeDistribution,
      List<ToiletColorCount> colorCount,
      ToiletPainDistribution painDistribution,
      List<ToiletPeriodCount> periodCount) {
    return new MonthlyToiletReport(
        size,
        scoreSummary,
        shapeCount,
        timeDistribution,
        colorCount,
        painDistribution,
        periodCount);
  }
}
