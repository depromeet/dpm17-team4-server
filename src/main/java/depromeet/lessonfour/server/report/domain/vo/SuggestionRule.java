package depromeet.lessonfour.server.report.domain.vo;

import java.util.List;

public interface SuggestionRule {
  void evaluate(SuggestionContext context, List<SuggestionType> types);
}
