package depromeet.lessonfour.server.report.domain.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.report.domain.vo.activity.MonthlyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.activity.WeeklyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.suggestion.SuggestionContext;
import depromeet.lessonfour.server.report.domain.vo.suggestion.SuggestionRule;
import depromeet.lessonfour.server.report.domain.vo.suggestion.SuggestionType;
import depromeet.lessonfour.server.report.domain.vo.toilet.MonthlyToiletReport;
import depromeet.lessonfour.server.report.domain.vo.toilet.WeeklyToiletReport;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SuggestionService {

  private final List<SuggestionRule> rules;

  public List<SuggestionType> suggest(
      WeeklyActivityReport weeklyActivityReport, WeeklyToiletReport weeklyToiletReport) {
    SuggestionContext context = SuggestionContext.weekly(weeklyActivityReport, weeklyToiletReport);
    List<SuggestionType> result = new ArrayList<>();
    rules.forEach(rule -> rule.evaluate(context, result));

    return result;
  }

  public List<SuggestionType> suggest(
      MonthlyActivityReport monthlyActivityReport, MonthlyToiletReport monthlyToiletReport) {
    SuggestionContext context =
        SuggestionContext.monthly(monthlyActivityReport, monthlyToiletReport);
    List<SuggestionType> result = new ArrayList<>();
    rules.forEach(rule -> rule.evaluate(context, result));

    return result;
  }
}
