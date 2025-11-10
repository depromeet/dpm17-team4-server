package depromeet.lessonfour.server.report.api.dto.response;

import java.util.List;

public record SuggestionSection(String message, List<SuggestionItem> items) {
  public record SuggestionItem(String image, String title, String content) {}
}
