package depromeet.lessonfour.server.report.app.service;

import java.time.LocalDate;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.report.domain.entity.ToiletScore;
import depromeet.lessonfour.server.report.domain.repository.ToiletScoreRepository;
import depromeet.lessonfour.server.report.domain.vo.monthly.ScoreSummary;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ToiletScoreService {

  private final ToiletScoreRepository toiletScoreRepository;

  @Transactional(readOnly = true)
  public int getScoreByActivityAt(Long userId, ActivityAt date) {
    return toiletScoreRepository.getScoreByActivityAt(userId, date);
  }

  @Transactional(readOnly = true)
  public ScoreSummary getMonthlyScoreSummary(Long userId, ActivityAt start, ActivityAt end) {
    Optional<ToiletScore> bestOpt = toiletScoreRepository.findMaxScoreBetween(userId, start, end);
    Optional<ToiletScore> worstOpt = toiletScoreRepository.findMinScoreBetween(userId, start, end);

    return new ScoreSummary(bestOpt.orElse(null), worstOpt.orElse(null));
  }

  @Transactional
  public void updateScore(Long userId, int score, ActivityAt activityAt) {
    LocalDate date = activityAt.toDate();

    toiletScoreRepository
        .findByDate(userId, date)
        .ifPresentOrElse(
            existing -> existing.updateScore(score),
            () -> toiletScoreRepository.save(ToiletScore.of(userId, score, date)));
  }

  // 월간 배변 점수 통계: 전체 평균 + 1~5주차 평균 리스트
  @Transactional(readOnly = true)
  public MonthlyScoreStats getMonthlyScoreStats(Long userId, ActivityAt start, ActivityAt end) {
    List<ToiletScore> scores = toiletScoreRepository.findAllBetween(userId, start, end);

    if (scores.isEmpty()) {
      // 평균 0, 주차별 0으로 채운 5칸
      List<Integer> weeklyZeros = IntStream.rangeClosed(1, 5).map(i -> 0).boxed().toList();
      return new MonthlyScoreStats(0.0, weeklyZeros);
    }

    // 전체 평균
    double average = scores.stream().mapToInt(ToiletScore::getScore).average().orElse(0.0);

    // 날짜 → 주차 인덱스(1~5)
    Map<Integer, List<ToiletScore>> scoresByWeek =
        scores.stream()
            .collect(
                Collectors.groupingBy(
                    s -> {
                      LocalDate date = s.getDate();
                      int dayOfMonth = date.getDayOfMonth();
                      return ((dayOfMonth - 1) / 7) + 1; // 1~5주차
                    }));

    // 1~5주차 평균 점수, 비어있으면 0
    List<Integer> weeklyAverageScores =
        IntStream.rangeClosed(1, 5)
            .mapToObj(
                week -> {
                  List<ToiletScore> weekScores = scoresByWeek.getOrDefault(week, List.of());
                  if (weekScores.isEmpty()) {
                    return 0;
                  }
                  IntSummaryStatistics stats =
                      weekScores.stream().mapToInt(ToiletScore::getScore).summaryStatistics();
                  return (int) Math.round(stats.getAverage());
                })
            .toList();

    return new MonthlyScoreStats(average, weeklyAverageScores);
  }

  public record MonthlyScoreStats(double averageScore, List<Integer> weeklyAverageScores) {}
}
