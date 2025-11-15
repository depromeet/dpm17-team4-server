package depromeet.lessonfour.server.report.app.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
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
  private final ToiletScoreService toiletScoreService;
  private final SuggestionService suggestionService;

  public WeeklyReport generateWeeklyReport(Long userId, LocalDateTime baseDateTime) {

    WeekRange thisWeek = WeekRange.of(baseDateTime.toLocalDate());
    WeekRange lastWeek = thisWeek.previous();

    // 주간 활동 리포트
    WeeklyActivityReport thisWeekActivity =
        activityReportService.generateWeeklyReport(userId, thisWeek);

    WeeklyActivityReport lastWeekActivity =
        activityReportService.generateWeeklyReport(userId, lastWeek);

    // 주간 배변 리포트 (DailyToiletReport 7개 기반 WeeklyToiletReport)
    WeeklyToiletReport thisWeekToilet = toiletReportService.generateWeeklyReport(userId, thisWeek);

    // 이번 주 점수 (월~일)
    List<Integer> thisWeekDailyScores =
        IntStream.range(0, 7)
            .mapToObj(
                i ->
                    toiletScoreService.getScoreByActivityAt(
                        userId, ActivityAt.of(thisWeek.start().plusDays(i))))
            .toList();

    double thisWeekAverageScore =
        thisWeekDailyScores.stream().mapToInt(Integer::intValue).average().orElse(0.0);

    // 지난 주 평균 점수
    List<Integer> lastWeekDailyScores =
        IntStream.range(0, 7)
            .mapToObj(
                i ->
                    toiletScoreService.getScoreByActivityAt(
                        userId, ActivityAt.of(lastWeek.start().plusDays(i))))
            .toList();

    double lastWeekAverageScore =
        lastWeekDailyScores.stream().mapToInt(Integer::intValue).average().orElse(0.0);

    // 주간 기준 습관 추천 (대표 하루 X, 주간 평균/횟수 기반)
    List<SuggestionType> suggestions = suggestionService.suggest(thisWeekActivity, thisWeekToilet);

    return new WeeklyReport(
        lastWeekAverageScore,
        thisWeekAverageScore,
        thisWeekDailyScores,
        lastWeekActivity,
        thisWeekActivity,
        suggestions);
  }
}
