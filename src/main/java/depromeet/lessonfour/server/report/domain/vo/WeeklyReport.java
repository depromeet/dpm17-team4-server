package depromeet.lessonfour.server.report.domain.vo;

import java.time.LocalDate;
import java.util.List;

import depromeet.lessonfour.server.report.domain.vo.activity.FoodsByMealTime;
import depromeet.lessonfour.server.report.domain.vo.activity.WeeklyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.suggestion.SuggestionType;

public record WeeklyReport(
    double lastWeekAverageScore,
    double thisWeekAverageScore,
    List<Integer> dailyScores, // 이번 주 월~일 점수
    WeeklyActivityReport lastWeekActivity, // 지난 주 액티비티 요약
    WeeklyActivityReport thisWeekActivity, // 이번 주 액티비티 요약
    List<SuggestionType> suggestions // 주간 습관 제안
    ) {

  public boolean hasFoodRecords() {
    return thisWeekActivity.hasFoodRecords();
  }

  public boolean hasDangerousFood() {
    return thisWeekActivity.hasDangerousFood();
  }

  public int getLastWeekDangerousFoodDays() {
    return lastWeekActivity.getLastWeekDangerousFoodDays();
  }

  public int getThisWeekDangerousFoodDays() {
    return thisWeekActivity.getThisWeekDangerousFoodDays();
  }

  public List<FoodsByMealTime> aggregateFoodsByMealTime(LocalDate weekStartDate) {
    return thisWeekActivity.aggregateFoodsByMealTime(weekStartDate);
  }
}
