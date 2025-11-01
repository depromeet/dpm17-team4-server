package depromeet.lessonfour.server.report.domain.vo;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletColor;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletShape;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StoolReport {

  private final double stoolScore;
  private final StoolEvaluationLevel level;
  private final List<StoolEvaluation> items;

  private static StoolReport empty() {
    return new StoolReport(0, StoolEvaluationLevel.NONE, List.of());
  }

  public static StoolReport summarize(List<StoolEvaluation> evaluations) {
    if (evaluations.isEmpty()) {
      return empty();
    }

    double averageScore =
        evaluations.stream()
            .map(StoolEvaluation::getScore)
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0);

    StoolEvaluationLevel level = StoolEvaluationLevel.from((int) (averageScore));

    return new StoolReport(averageScore, level, evaluations);
  }

  public boolean hasBlood() {
    return items.stream().map(StoolEvaluation::getColor).anyMatch(c -> c == ToiletColor.RED);
  }

  public boolean hasAbnormalColor() {
    return items.stream()
        .map(StoolEvaluation::getColor)
        .anyMatch(c -> c == ToiletColor.GREEN || c == ToiletColor.GRAY);
  }

  public boolean drunkAlcohol() {
    return items.stream()
        .anyMatch(
            item -> {
              String note = item.getNote();
              if (note == null || note.isBlank()) {
                return false;
              }
              return note.contains("술") || note.contains("음주") || note.contains("과음");
            });
  }

  public double getAverageDuration() {
    return items.stream().mapToInt(StoolEvaluation::getDuration).average().orElse(0);
  }

  public double getAveragePain() {
    return items.stream().mapToDouble(StoolEvaluation::getPain).average().orElse(0);
  }

  public int getNumberOfRecords() {
    return items.size();
  }

  public ToiletShape getMostFrequentShape() {
    return items.stream()
        .map(StoolEvaluation::getShape)
        .filter(Objects::nonNull)
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
        .entrySet()
        .stream()
        .max(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .orElse(null);
  }

  public ToiletColor getMostFrequentColor() {
    return items.stream()
        .map(StoolEvaluation::getColor)
        .filter(Objects::nonNull)
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
        .entrySet()
        .stream()
        .max(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .orElse(null);
  }

  public boolean hasGoodShape() {
    ToiletShape shape = getMostFrequentShape();
    return shape == ToiletShape.BANANA;
  }

  public boolean hasGoodColor() {
    ToiletColor color = getMostFrequentColor();
    return color == ToiletColor.DARK_BROWN || color == ToiletColor.GOLD;
  }
}
