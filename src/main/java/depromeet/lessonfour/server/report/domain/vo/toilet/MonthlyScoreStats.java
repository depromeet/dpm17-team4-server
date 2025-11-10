package depromeet.lessonfour.server.report.domain.vo.toilet;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import depromeet.lessonfour.server.common.api.code.ErrorCode;
import depromeet.lessonfour.server.common.exception.ServerException;
import depromeet.lessonfour.server.report.domain.entity.ToiletScore;

public record MonthlyScoreStats(List<ToiletScore> scores) {

  public MonthlyScoreStats {
    if (scores == null) {
      throw new ServerException(ErrorCode.INSUFFICIENT_DATA_FOR_REPORT);
    }
    scores = List.copyOf(scores);
  }

  public double getAverageScore() {
    return scores.stream()
        .mapToInt(ToiletScore::getScore)
        .average()
        .orElseThrow(() -> new ServerException(ErrorCode.INSUFFICIENT_DATA_FOR_REPORT));
  }

  public List<Integer> getWeeklyAverageScore() {
    if (scores.isEmpty()) {
      // 2주차 이상의 데이터 필요
      throw new ServerException(ErrorCode.INSUFFICIENT_DATA_FOR_REPORT);
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

  public ToiletScore getMaxScore() {
    return scores.stream()
        .max(Comparator.comparingInt(ToiletScore::getScore))
        .orElseThrow(() -> new ServerException(ErrorCode.INSUFFICIENT_DATA_FOR_REPORT));
  }

  public ToiletScore getMinScore() {
    return scores.stream()
        .min(Comparator.comparingInt(ToiletScore::getScore))
        .orElseThrow(() -> new ServerException(ErrorCode.INSUFFICIENT_DATA_FOR_REPORT));
  }
}
