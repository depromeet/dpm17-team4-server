package depromeet.lessonfour.server.report.app.service;

import java.time.LocalDateTime;

import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.report.app.dto.response.DailyReport;
import depromeet.lessonfour.server.report.domain.service.SuggestionService;
import depromeet.lessonfour.server.report.domain.vo.DailyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.DailyToiletReport;
import depromeet.lessonfour.server.report.domain.vo.Suggestion;
import lombok.RequiredArgsConstructor;

@UseCase
@Transactional
@RequiredArgsConstructor
public class GetDailyReportUseCase {

  private final ActivityReportService activityReportService;
  private final ToiletReportService toiletReportService;
  private final SuggestionService suggestionService;

  public DailyReport getDailyReport(Long userId, LocalDateTime dateTime) {
    DailyActivityReport dailyActivityReport =
        activityReportService.generateDailyReport(userId, dateTime);
    DailyToiletReport dailyToiletReport = toiletReportService.generateDailyReport(userId, dateTime);
    Suggestion suggestion = suggestionService.suggest(dailyActivityReport, dailyToiletReport);

    return new DailyReport(dailyActivityReport, dailyToiletReport, suggestion);
  }
}
