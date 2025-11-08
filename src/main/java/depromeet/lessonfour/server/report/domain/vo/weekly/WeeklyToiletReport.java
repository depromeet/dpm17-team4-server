package depromeet.lessonfour.server.report.domain.vo.weekly;

import java.util.List;

import depromeet.lessonfour.server.report.domain.vo.DailyToiletReport;
import depromeet.lessonfour.server.report.domain.vo.ToiletEvaluationLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WeeklyToiletReport {

  /** 해당 주(월~일)의 DailyToiletReport 리스트 */
  private final List<DailyToiletReport> dailyReports;

  public int size() {
    return dailyReports != null ? dailyReports.size() : 0;
  }

  /** 주간 평균 배변 점수 */
  public double getAverageScore() {
    if (dailyReports == null || dailyReports.isEmpty()) {
      return 0;
    }
    return dailyReports.stream().mapToDouble(DailyToiletReport::getToiletScore).average().orElse(0);
  }

  public ToiletEvaluationLevel getLevel() {
    return ToiletEvaluationLevel.from(getAverageScore());
  }

  public boolean hasBlood() {
    return dailyReports != null && dailyReports.stream().anyMatch(DailyToiletReport::hasBlood);
  }

  public boolean hasAbnormalColor() {
    return dailyReports != null
        && dailyReports.stream().anyMatch(DailyToiletReport::hasAbnormalColor);
  }

  public boolean drunkAlcohol() {
    return dailyReports != null && dailyReports.stream().anyMatch(DailyToiletReport::drunkAlcohol);
  }

  public double getAverageDuration() {
    if (dailyReports == null || dailyReports.isEmpty()) {
      return 0;
    }
    return dailyReports.stream()
        .mapToDouble(DailyToiletReport::getAverageDuration)
        .average()
        .orElse(0);
  }

  public double getAveragePain() {
    if (dailyReports == null || dailyReports.isEmpty()) {
      return 0;
    }
    return dailyReports.stream().mapToDouble(DailyToiletReport::getAveragePain).average().orElse(0);
  }

  public int getNumberOfRecords() {
    if (dailyReports == null) {
      return 0;
    }
    return dailyReports.stream().mapToInt(DailyToiletReport::getNumberOfRecords).sum();
  }

  public boolean hasGoodShape() {
    return dailyReports != null && dailyReports.stream().anyMatch(DailyToiletReport::hasGoodShape);
  }

  public boolean hasGoodColor() {
    return dailyReports != null && dailyReports.stream().anyMatch(DailyToiletReport::hasGoodColor);
  }
}
