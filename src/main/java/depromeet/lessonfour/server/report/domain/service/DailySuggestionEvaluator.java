package depromeet.lessonfour.server.report.domain.service;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.report.domain.vo.SuggestionEvaluation;

@Component
public class DailySuggestionEvaluator {

  public SuggestionEvaluation evaluate() {
    return new SuggestionEvaluation();
  }
}
