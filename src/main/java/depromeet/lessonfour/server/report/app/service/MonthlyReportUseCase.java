package depromeet.lessonfour.server.report.app.service;

import java.time.YearMonth;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.report.app.dto.response.MonthlyReport;
import depromeet.lessonfour.server.report.domain.service.SuggestionService;
import depromeet.lessonfour.server.report.domain.vo.activity.MonthlyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.suggestion.SuggestionType;
import depromeet.lessonfour.server.report.domain.vo.toilet.MonthlyScoreStats;
import depromeet.lessonfour.server.report.domain.vo.toilet.MonthlyToiletReport;
import lombok.RequiredArgsConstructor;

@UseCase
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MonthlyReportUseCase {

  private final ToiletReportService toiletReportService;
  private final ActivityReportService activityReportService;
  private final SuggestionService suggestionService;
  private final ToiletScoreService toiletScoreService;

  public MonthlyReport generateMonthlyReport(Long userId, YearMonth month) {
    ActivityAt monthStart = ActivityAt.of(month.atDay(1));
    ActivityAt monthEndExclusive = ActivityAt.of(month.atEndOfMonth().plusDays(1));

    // 배변 기록
    MonthlyToiletReport toiletReport =
        toiletReportService.generateMonthlyReport(userId, monthStart, monthEndExclusive);

    // 생활 기록
    MonthlyActivityReport activityReport =
        activityReportService.generateMonthlyReport(userId, monthStart, monthEndExclusive);

    // 배변 점수
    MonthlyScoreStats scoreStats =
        toiletScoreService.getScoresByActivityAtBetween(userId, monthStart, monthEndExclusive);

    // 추천 습관
    List<SuggestionType> suggestions = suggestionService.suggest(activityReport, toiletReport);

    return new MonthlyReport(scoreStats, suggestions, toiletReport, activityReport);
  }
}
