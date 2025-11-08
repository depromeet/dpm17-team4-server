package depromeet.lessonfour.server.report.app.service;

import java.time.LocalDateTime;

import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
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
    ActivityAt activityAt = ActivityAt.from(dateTime);

    // 생활 기록 리포트 생성
    DailyActivityReport dailyActivityReport =
        activityReportService.generateDailyReport(userId, activityAt);

    // 배변 기록 리포트 생성
    DailyToiletReport dailyToiletReport =
        toiletReportService.generateDailyReport(userId, activityAt);

    // 맞춤형 제안 생성
    Suggestion suggestion = suggestionService.suggest(dailyActivityReport, dailyToiletReport);

    return new DailyReport(dailyActivityReport, dailyToiletReport, suggestion);
  }
}
