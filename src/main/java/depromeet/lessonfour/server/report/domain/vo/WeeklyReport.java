package depromeet.lessonfour.server.report.domain.vo;

import java.util.List;

import depromeet.lessonfour.server.report.domain.vo.activity.WeeklyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.suggestion.SuggestionType;
import depromeet.lessonfour.server.report.domain.vo.toilet.WeeklyToiletReport;

public record WeeklyReport(
    WeeklyToiletReport lastWeekToilet,
    WeeklyToiletReport thisWeekToilet,
    WeeklyActivityReport lastWeekActivity,
    WeeklyActivityReport thisWeekActivity,
    List<SuggestionType> suggestions // 주간 습관 제안
    ) {

  /** 이번주 평균 점수 */
  public double getThisWeekAverageScore() {
    return thisWeekToilet.getAverageScore();
  }

  /** 지난주 평균 점수 */
  public double getLastWeekAverageScore() {
    return lastWeekToilet.getAverageScore();
  }

  /** 이번주 점수 리스트 */
  public List<Double> getScoresThisWeek() {
    return thisWeekToilet.getScores();
  }
}
