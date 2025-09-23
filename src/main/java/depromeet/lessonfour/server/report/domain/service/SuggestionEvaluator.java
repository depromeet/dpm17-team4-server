package depromeet.lessonfour.server.report.domain.service;

import depromeet.lessonfour.server.report.domain.vo.ReportPeriod;
import depromeet.lessonfour.server.report.domain.vo.ReportType;
import depromeet.lessonfour.server.report.domain.vo.SuggestionEvaluation;
import org.springframework.stereotype.Component;

@Component
public class SuggestionEvaluator {

  public ReportType getKey() {
    return ReportType.SUGGESTION;
  }

  public SuggestionEvaluation evaluate(Long userId, ReportPeriod period) {
    return null;
  }
}
