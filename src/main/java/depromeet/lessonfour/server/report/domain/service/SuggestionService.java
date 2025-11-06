package depromeet.lessonfour.server.report.domain.service;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.report.domain.policy.SuggestionPolicy;
import depromeet.lessonfour.server.report.domain.vo.ActivityReport;
import depromeet.lessonfour.server.report.domain.vo.Suggestion;
import depromeet.lessonfour.server.report.domain.vo.ToiletReport;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SuggestionService {

  private final SuggestionPolicy suggestionPolicy;

  public Suggestion suggest(ActivityReport activityReport, ToiletReport toiletReport) {
    return suggestionPolicy.evaluate(activityReport, toiletReport);
  }
}
