package depromeet.lessonfour.server.report.app.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.common.domain.vo.WeekRange;
import depromeet.lessonfour.server.report.domain.service.SuggestionService;
import depromeet.lessonfour.server.report.domain.vo.WeeklyReport;
import depromeet.lessonfour.server.report.domain.vo.activity.WeeklyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.suggestion.SuggestionType;
import depromeet.lessonfour.server.report.domain.vo.toilet.WeeklyToiletReport;
import lombok.RequiredArgsConstructor;

@UseCase
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WeeklyReportUseCase {

  private final ToiletReportService toiletReportService;
  private final ActivityReportService activityReportService;
  private final SuggestionService suggestionService;

  public WeeklyReport generateWeeklyReport(Long userId, LocalDateTime baseDateTime) {

    WeekRange thisWeek = WeekRange.of(baseDateTime.toLocalDate());
    WeekRange lastWeek = thisWeek.previous();

    // 주간 생활기록 리포트
    WeeklyActivityReport thisWeekActivity =
        activityReportService.generateWeeklyReport(userId, thisWeek);
    WeeklyActivityReport lastWeekActivity =
        activityReportService.generateWeeklyReport(userId, lastWeek);

    // 주간 배변기록 리포트
    WeeklyToiletReport thisWeekToilet = toiletReportService.generateWeeklyReport(userId, thisWeek);
    WeeklyToiletReport lastWeekToilet = toiletReportService.generateWeeklyReport(userId, lastWeek);

    // 주간 기준 습관 추천 (대표 하루 X, 주간 평균/횟수 기반)
    List<SuggestionType> suggestions = suggestionService.suggest(thisWeekActivity, thisWeekToilet);

    return new WeeklyReport(
        lastWeekToilet, thisWeekToilet, lastWeekActivity, thisWeekActivity, suggestions);
  }
}
