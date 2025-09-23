package depromeet.lessonfour.server.report.domain.service;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.report.domain.policy.SuggestionPolicy;
import depromeet.lessonfour.server.report.domain.vo.ActivityReport;
import depromeet.lessonfour.server.report.domain.vo.StoolReport;
import depromeet.lessonfour.server.report.domain.vo.Suggestion;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SuggestionService {

  private final SuggestionPolicy suggestionPolicy;

  public Suggestion suggest(ActivityReport activityReport, StoolReport stoolReport) {
    return suggestionPolicy.evaluate(activityReport, stoolReport);
  }
}
