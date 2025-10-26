package depromeet.lessonfour.server.report.app.service;

import java.time.LocalDateTime;

import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.report.app.dto.response.DailyReport;
import depromeet.lessonfour.server.report.domain.service.SuggestionService;
import depromeet.lessonfour.server.report.domain.vo.ActivityReport;
import depromeet.lessonfour.server.report.domain.vo.StoolReport;
import depromeet.lessonfour.server.report.domain.vo.Suggestion;
import lombok.RequiredArgsConstructor;

@UseCase
@Transactional
@RequiredArgsConstructor
public class GetDailyReportUseCase {

  private final ActivityReportService activityReportService;
  private final StoolReportService stoolReportService;
  private final SuggestionService suggestionService;

  public DailyReport getDailyReport(Long userId, LocalDateTime dateTime) {
    ActivityReport activityReport = activityReportService.generateDailyReport(userId, dateTime);
    StoolReport stoolReport = stoolReportService.generateDailyReport(userId, dateTime);
    Suggestion suggestion = suggestionService.suggest(activityReport, stoolReport);

    return new DailyReport(activityReport, stoolReport, suggestion);
  }
}
