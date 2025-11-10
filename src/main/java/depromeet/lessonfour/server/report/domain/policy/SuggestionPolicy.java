package depromeet.lessonfour.server.report.domain.policy;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.report.domain.vo.SuggestionContext;
import depromeet.lessonfour.server.report.domain.vo.SuggestionRule;
import depromeet.lessonfour.server.report.domain.vo.SuggestionType;
import depromeet.lessonfour.server.report.domain.vo.monthly.MonthlyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.monthly.MonthlyToiletReport;
import depromeet.lessonfour.server.report.domain.vo.weekly.WeeklyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.weekly.WeeklyToiletReport;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SuggestionPolicy {

  List<SuggestionRule> rules;

  public List<SuggestionType> evaluateWeekly(
      WeeklyActivityReport weeklyActivityReport, WeeklyToiletReport weeklyToiletReport) {
    SuggestionContext context = SuggestionContext.weekly(weeklyActivityReport, weeklyToiletReport);
    List<SuggestionType> result = new ArrayList<>();
    rules.forEach(rule -> rule.evaluate(context, result));

    return result;
  }

  public List<SuggestionType> evaluateMonthly(
      MonthlyActivityReport monthlyActivityReport, MonthlyToiletReport monthlyToiletReport) {
    return List.of();
  }
}
