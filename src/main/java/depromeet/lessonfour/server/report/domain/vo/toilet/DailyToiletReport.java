package depromeet.lessonfour.server.report.domain.vo.toilet;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DailyToiletReport {

  private final double toiletScore;
  private final ToiletEvaluationLevel level;
  private final List<ToiletEvaluation> items;

  private static DailyToiletReport empty() {
    return new DailyToiletReport(0, ToiletEvaluationLevel.NONE, List.of());
  }

  public static DailyToiletReport summarize(List<ToiletEvaluation> evaluations) {
    if (evaluations.isEmpty()) {
      return empty();
    }

    double averageScore =
        evaluations.stream()
            .map(ToiletEvaluation::getScore)
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0);

    ToiletEvaluationLevel level = ToiletEvaluationLevel.from((int) (averageScore));

    return new DailyToiletReport(averageScore, level, evaluations);
  }
}
