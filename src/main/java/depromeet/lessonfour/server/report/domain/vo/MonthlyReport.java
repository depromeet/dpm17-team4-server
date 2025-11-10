package depromeet.lessonfour.server.report.domain.vo;

import java.util.List;

import depromeet.lessonfour.server.report.domain.entity.ToiletScore;
import depromeet.lessonfour.server.report.domain.vo.activity.MonthlyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.activity.StressEvaluation;
import depromeet.lessonfour.server.report.domain.vo.suggestion.SuggestionType;
import depromeet.lessonfour.server.report.domain.vo.toilet.MonthlyScoreStats;
import depromeet.lessonfour.server.report.domain.vo.toilet.MonthlyToiletReport;
import depromeet.lessonfour.server.report.domain.vo.toilet.ToiletColorCount;
import depromeet.lessonfour.server.report.domain.vo.toilet.ToiletPainDistribution;
import depromeet.lessonfour.server.report.domain.vo.toilet.ToiletPeriodCount;
import depromeet.lessonfour.server.report.domain.vo.toilet.ToiletShapeCount;
import depromeet.lessonfour.server.report.domain.vo.toilet.ToiletTimeDistribution;

public record MonthlyReport(
    MonthlyScoreStats scores,
    List<SuggestionType> suggestions,
    MonthlyToiletReport toiletReport,
    MonthlyActivityReport activityReport) {

  public int getToiletRecordCount() {
    return toiletReport.size();
  }

  public int getActivityRecordCount() {
    return activityReport.size();
  }

  public int getTotalCount() {
    return getToiletRecordCount() + getActivityRecordCount();
  }

  public List<Integer> getWeeklyAverageScore() {
    return scores.getWeeklyAverageScore();
  }

  public double getAverageScore() {
    return scores.getAverageScore();
  }

  public List<ToiletShapeCount> getMostFrequentToiletShapes() {
    return toiletReport.getMostShape();
  }

  public List<ToiletColorCount> getMostFrequentToiletColors() {
    return toiletReport.getMostColor();
  }

  public ToiletTimeDistribution getToiletTimeDistribution() {
    return toiletReport.getTimeDistributions();
  }

  /** 배변 시간대 분포 count 기준 내림차순 정렬 */
  public List<ToiletPeriodCount> getToiletPeriodCounts() {
    return toiletReport.getPeriodDistribution();
  }

  public ToiletPainDistribution getToiletPainDistribution() {
    return toiletReport.getPainDistribution();
  }

  public ToiletScore getMaxToiletScore() {
    return scores.getMaxScore().orElseGet(ToiletScore::empty);
  }

  public ToiletScore getMinToiletScore() {
    return scores.getMinScore().orElseGet(ToiletScore::empty);
  }

  /** 주차별 평균 스트레스 점수 계산 */
  public List<MonthlyActivityReport.WeeklyAverageStress> getWeeklyAverageStress() {
    return activityReport.getWeeklyAverageStress();
  }

  /** 전체 월의 평균 스트레스 반환 */
  public StressEvaluation getMonthlyAverageStress() {
    return activityReport.getMonthlyAverageStress();
  }
}
