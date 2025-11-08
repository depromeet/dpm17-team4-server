package depromeet.lessonfour.server.report.app.service;

import java.time.YearMonth;

import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.report.app.dto.response.MonthlyReport;
import depromeet.lessonfour.server.report.app.dto.response.MonthlyScore;
import depromeet.lessonfour.server.report.app.dto.response.RecordCounts;
import depromeet.lessonfour.server.report.domain.service.SuggestionService;
import depromeet.lessonfour.server.report.domain.vo.Suggestion;
import depromeet.lessonfour.server.report.domain.vo.monthly.MonthlyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.monthly.MonthlyToiletReport;
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

    MonthlyToiletReport toiletReport =
        toiletReportService.generateMonthlyReport(userId, monthStart, monthEndExclusive);

    MonthlyActivityReport activityReport =
        activityReportService.generateMonthlyReport(userId, monthStart, monthEndExclusive);

    // 월간 배변 점수 통계 (전체 평균 + 주차별 평균)
    ToiletScoreService.MonthlyScoreStats scoreStats =
        toiletScoreService.getMonthlyScoreStats(userId, monthStart, monthEndExclusive);

    Suggestion suggestion = suggestionService.suggest(activityReport, toiletReport);

    return new MonthlyReport(
        RecordCounts.of(activityReport.size(), toiletReport.size()),
        MonthlyScore.from(toiletReport.scoreSummary()),
        scoreStats.averageScore(),
        scoreStats.weeklyAverageScores(),
        toiletReport.shapeCount(),
        toiletReport.timeDistribution(),
        toiletReport.colorCount(),
        toiletReport.painDistribution(),
        toiletReport.periodCount(),
        suggestion,
        activityReport);
  }
}
