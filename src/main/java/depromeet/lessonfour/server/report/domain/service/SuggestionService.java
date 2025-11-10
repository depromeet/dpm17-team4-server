package depromeet.lessonfour.server.report.domain.service;

import java.util.List;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.report.domain.policy.SuggestionPolicy;
import depromeet.lessonfour.server.report.domain.vo.SuggestionType;
import depromeet.lessonfour.server.report.domain.vo.monthly.MonthlyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.monthly.MonthlyToiletReport;
import depromeet.lessonfour.server.report.domain.vo.weekly.WeeklyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.weekly.WeeklyToiletReport;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SuggestionService {

  private final SuggestionPolicy suggestionPolicy;

  public List<SuggestionType> suggest(
      WeeklyActivityReport weeklyActivityReport, WeeklyToiletReport weeklyToiletReport) {
    return suggestionPolicy.evaluateWeekly(weeklyActivityReport, weeklyToiletReport);
  }

  public List<SuggestionType> suggest(
      MonthlyActivityReport monthlyActivityReport, MonthlyToiletReport monthlyToiletReport) {
    return suggestionPolicy.evaluateMonthly(monthlyActivityReport, monthlyToiletReport);
  }
}
