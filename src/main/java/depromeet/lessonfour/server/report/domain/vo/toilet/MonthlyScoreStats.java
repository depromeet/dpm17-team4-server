package depromeet.lessonfour.server.report.domain.vo.toilet;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import depromeet.lessonfour.server.report.domain.entity.ToiletScore;

public record MonthlyScoreStats(List<ToiletScore> scores) {

  public double getAverageScore() {
    return scores.stream().mapToInt(ToiletScore::getScore).average().orElse(0.0);
  }

  public List<Integer> getWeeklyAverageScore() {
    if (scores.isEmpty()) {
      // 평균 0, 주차별 0으로 채운 5칸
      return IntStream.rangeClosed(1, 5).map(i -> 0).boxed().toList();
    }

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
    return IntStream.rangeClosed(1, 5)
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
  }

  public Optional<ToiletScore> getMaxScore() {
    return scores.stream().max(Comparator.comparingInt(ToiletScore::getScore));
  }

  public Optional<ToiletScore> getMinScore() {
    return scores.stream().min(Comparator.comparingInt(ToiletScore::getScore));
  }
}
