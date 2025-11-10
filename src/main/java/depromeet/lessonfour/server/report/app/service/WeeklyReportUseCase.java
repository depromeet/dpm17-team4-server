package depromeet.lessonfour.server.report.app.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.report.app.dto.response.WeeklyReport;
import depromeet.lessonfour.server.report.domain.service.SuggestionService;
import depromeet.lessonfour.server.report.domain.vo.SuggestionType;
import depromeet.lessonfour.server.report.domain.vo.weekly.WeeklyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.weekly.WeeklyToiletReport;
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
    LocalDate baseDate = baseDateTime.toLocalDate();

    // 이번 주: 월요일 ~ 다음 주 월요일 직전까지 [start, end)
    LocalDate thisWeekStartDate = baseDate.with(DayOfWeek.MONDAY);
    LocalDate thisWeekEndExclusiveDate = thisWeekStartDate.plusDays(7);

    // 지난 주: 이번 주 시작 -7일 ~ 이번 주 시작 [start, end)
    LocalDate lastWeekStartDate = thisWeekStartDate.minusWeeks(1);

    ActivityAt thisWeekStart = ActivityAt.of(thisWeekStartDate);
    ActivityAt thisWeekEndExclusive = ActivityAt.of(thisWeekEndExclusiveDate);

    ActivityAt lastWeekStart = ActivityAt.of(lastWeekStartDate);
    ActivityAt lastWeekEndExclusive = ActivityAt.of(thisWeekStartDate);

    // 주간 활동 리포트
    WeeklyActivityReport thisWeekActivity =
        activityReportService.generateWeeklyReport(userId, thisWeekStart, thisWeekEndExclusive);

    WeeklyActivityReport lastWeekActivity =
        activityReportService.generateWeeklyReport(userId, lastWeekStart, lastWeekEndExclusive);

    // 주간 배변 리포트 (DailyToiletReport 7개 기반 WeeklyToiletReport)
    WeeklyToiletReport thisWeekToilet =
        toiletReportService.generateWeeklyReport(userId, thisWeekStart, thisWeekEndExclusive);

    // 이번 주 점수 (월~일)
    List<Integer> thisWeekDailyScores =
        IntStream.range(0, 7)
            .mapToObj(
                i ->
                    toiletScoreService.getScoreByActivityAt(
                        userId, ActivityAt.of(thisWeekStartDate.plusDays(i))))
            .toList();

    double thisWeekAverageScore =
        thisWeekDailyScores.stream().mapToInt(Integer::intValue).average().orElse(0.0);

    // 지난 주 평균 점수
    List<Integer> lastWeekDailyScores =
        IntStream.range(0, 7)
            .mapToObj(
                i ->
                    toiletScoreService.getScoreByActivityAt(
                        userId, ActivityAt.of(lastWeekStartDate.plusDays(i))))
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
