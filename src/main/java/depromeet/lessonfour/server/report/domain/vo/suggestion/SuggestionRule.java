package depromeet.lessonfour.server.report.domain.vo.suggestion;

import java.util.List;

public interface SuggestionRule {
  void evaluate(SuggestionContext context, List<SuggestionType> types);
}
