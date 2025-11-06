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
public class ToiletReport {

  private final double stoolScore;
  private final ToiletEvaluationLevel level;
  private final List<ToiletEvaluation> items;

  private static ToiletReport empty() {
    return new ToiletReport(0, ToiletEvaluationLevel.NONE, List.of());
  }

  public static ToiletReport summarize(List<ToiletEvaluation> evaluations) {
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

    return new ToiletReport(averageScore, level, evaluations);
  }

  public boolean hasBlood() {
    return items.stream().anyMatch(item -> item.getColor() == ToiletColor.RED);
  }

  public boolean hasAbnormalColor() {
    return items.stream()
        .anyMatch(
            item -> item.getColor() == ToiletColor.GREEN || item.getColor() == ToiletColor.GRAY);
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
    return items.stream().mapToInt(ToiletEvaluation::getDuration).average().orElse(0);
  }

  public double getAveragePain() {
    return items.stream().mapToDouble(ToiletEvaluation::getPain).average().orElse(0);
  }

  public int getNumberOfRecords() {
    return items.size();
  }

  public ToiletShape getMostFrequentShape() {
    return items.stream()
        .map(ToiletEvaluation::getShape)
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
        .entrySet()
        .stream()
        .max(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .orElse(null);
  }

  public ToiletColor getMostFrequentColor() {
    return items.stream()
        .map(ToiletEvaluation::getColor)
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
        .entrySet()
        .stream()
        .max(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .orElse(null);
  }

  public boolean hasGoodShape() {
    return getMostFrequentShape() == ToiletShape.BANANA;
  }

  public boolean hasGoodColor() {
    ToiletColor color = getMostFrequentColor();
    return color == ToiletColor.DARK_BROWN || color == ToiletColor.GOLD;
  }
}
