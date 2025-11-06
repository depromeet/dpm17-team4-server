package depromeet.lessonfour.server.report.domain.service;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.report.domain.policy.SuggestionPolicy;
import depromeet.lessonfour.server.report.domain.vo.DailyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.DailyToiletReport;
import depromeet.lessonfour.server.report.domain.vo.Suggestion;
import depromeet.lessonfour.server.report.domain.vo.monthly.MonthlyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.monthly.MonthlyToiletReport;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SuggestionService {

  private final SuggestionPolicy suggestionPolicy;

  public Suggestion suggest(
      DailyActivityReport dailyActivityReport, DailyToiletReport dailyToiletReport) {
    return suggestionPolicy.evaluate(dailyActivityReport, dailyToiletReport);
  }

  public Suggestion suggest(
      MonthlyActivityReport monthlyActivityReport, MonthlyToiletReport monthlyToiletReport) {
    return Suggestion.dummy();
  }
}
