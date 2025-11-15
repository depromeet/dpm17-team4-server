package depromeet.lessonfour.server.report.domain.vo.toilet;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import depromeet.lessonfour.server.common.api.code.ErrorCode;
import depromeet.lessonfour.server.common.exception.ServerException;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletColor;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletShape;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;

@AllArgsConstructor
@Builder(access = AccessLevel.PRIVATE)
public class MonthlyToiletReport {

  private List<WeeklyToiletReport> weeklyReports;
  private long lastMonthPainfulDays;

  public static MonthlyToiletReport summarize(
      List<ToiletRecord> records, long lastMonthPainfulDays) {

    WeekFields wf = WeekFields.ISO;
    Map<Integer, List<ToiletRecord>> byWeekOfMonth =
        records.stream()
            .collect(
                Collectors.groupingBy(
                    r -> {
                      LocalDate d = r.getActivityAt().toDate();
                      return d.get(wf.weekOfMonth()); // 0 ~ 5(또는 6)주차
                    }));

    List<WeeklyToiletReport> weeklyReports =
        byWeekOfMonth.entrySet().stream()
            .sorted(Entry.comparingByKey()) // 주차 오름차순
            .map(e -> WeeklyToiletReport.summarize(e.getValue()))
            .toList();

    hasMoreThanOneWeeklyReport(weeklyReports);

    return MonthlyToiletReport.builder()
        .weeklyReports(weeklyReports)
        .lastMonthPainfulDays(lastMonthPainfulDays)
        .build();
  }

  private static void hasMoreThanOneWeeklyReport(List<WeeklyToiletReport> weeklyReports) {
    if (weeklyReports.size() < 2) {
      throw new ServerException(ErrorCode.INSUFFICIENT_DATA_FOR_REPORT);
    }
  }

  public int size() {
    return weeklyReports.stream().mapToInt(WeeklyToiletReport::size).sum();
  }

  public List<ToiletEvaluation> getAllEvaluations() {
    return weeklyReports.stream()
        .flatMap(weeklyReport -> weeklyReport.getDailyReports().stream())
        .flatMap(dailyReport -> dailyReport.getItems().stream())
        .toList();
  }

  public List<ToiletShapeCount> getMostShape() {
    Map<ToiletShape, Long> shapeCountMap =
        weeklyReports.stream()
            .flatMap(weeklyReport -> weeklyReport.getDailyReports().stream())
            .flatMap(dailyReport -> dailyReport.getItems().stream())
            .filter(evaluation -> evaluation.getShape() != null)
            .filter(evaluation -> evaluation.getShape() != ToiletShape.NONE)
            .collect(Collectors.groupingBy(ToiletEvaluation::getShape, Collectors.counting()));

    return shapeCountMap.entrySet().stream()
        .sorted(Map.Entry.<ToiletShape, Long>comparingByValue().reversed())
        .limit(3)
        .map(entry -> new ToiletShapeCount(entry.getKey(), entry.getValue().intValue()))
        .toList();
  }

  /** 가장 많이 본 배변 색상 상위 3개를 반환합니다. 정렬 순서 : 빈도수 내림차순 */
  public List<ToiletColorCount> getMostColor() {
    Map<ToiletColor, Long> colorMap =
        weeklyReports.stream()
            .flatMap(weeklyReport -> weeklyReport.getDailyReports().stream())
            .flatMap(dailyReport -> dailyReport.getItems().stream())
            .filter(evaluation -> evaluation.getColor() != null)
            .filter(evaluation -> evaluation.getColor() != ToiletColor.NONE)
            .collect(Collectors.groupingBy(ToiletEvaluation::getColor, Collectors.counting()));

    return colorMap.entrySet().stream()
        .sorted(Map.Entry.<ToiletColor, Long>comparingByValue().reversed())
        .limit(3)
        .map(entry -> new ToiletColorCount(entry.getKey(), entry.getValue().intValue()))
        .toList();
  }

  public ToiletTimeDistribution getTimeDistributions() {
    Map<String, Long> timeGroups =
        weeklyReports.stream()
            .flatMap(weeklyReport -> weeklyReport.getDailyReports().stream())
            .flatMap(dailyReport -> dailyReport.getItems().stream())
            .collect(
                Collectors.groupingBy(
                    evaluation -> {
                      int duration = evaluation.getDuration();
                      if (duration <= 5) {
                        return "WITHIN_5";
                      } else if (duration <= 10) {
                        return "OVER_5";
                      } else {
                        return "OVER_10";
                      }
                    },
                    Collectors.counting()));

    int within5min = timeGroups.getOrDefault("WITHIN_5", 0L).intValue();
    int over5min = timeGroups.getOrDefault("OVER_5", 0L).intValue();
    int over10min = timeGroups.getOrDefault("OVER_10", 0L).intValue();

    return new ToiletTimeDistribution(within5min, over5min, over10min);
  }

  public ToiletPainDistribution getPainDistribution() {
    Map<String, Long> painMap =
        weeklyReports.stream()
            .flatMap(weeklyReport -> weeklyReport.getDailyReports().stream())
            .flatMap(dailyReport -> dailyReport.getItems().stream())
            .collect(
                Collectors.groupingBy(
                    evaluation -> {
                      double pain = evaluation.getPain();
                      if (pain < 10) {
                        return "VERY_LOW";
                      } else if (pain < 30) {
                        return "LOW";
                      } else if (pain < 50) {
                        return "MEDIUM";
                      } else if (pain < 70) {
                        return "HIGH";
                      } else {
                        return "VERY_HIGH";
                      }
                    },
                    Collectors.counting()));

    // 이번 달의 아팠던 횟수 계산
    long currentMonthPainCount =
        painMap.entrySet().stream()
            .filter(e -> "HIGH".equals(e.getKey()) || "VERY_HIGH".equals(e.getKey()))
            .mapToLong(Entry::getValue)
            .sum();

    int painDiff = (int) (currentMonthPainCount - lastMonthPainfulDays);

    return new ToiletPainDistribution(
        painMap.getOrDefault("VERY_LOW", 0L).intValue(),
        painMap.getOrDefault("LOW", 0L).intValue(),
        painMap.getOrDefault("MEDIUM", 0L).intValue(),
        painMap.getOrDefault("HIGH", 0L).intValue(),
        painMap.getOrDefault("VERY_HIGH", 0L).intValue(),
        painDiff);
  }

  public List<ToiletPeriodCount> getPeriodDistribution() {
    Map<DayPeriod, Long> periodCountMap =
        weeklyReports.stream()
            .flatMap(weeklyReport -> weeklyReport.getDailyReports().stream())
            .flatMap(dailyReport -> dailyReport.getItems().stream())
            .collect(
                Collectors.groupingBy(
                    evaluation -> {
                      LocalTime time = evaluation.getOccurredAt().getTime();

                      if (!time.isBefore(LocalTime.of(5, 0))
                          && time.isBefore(LocalTime.of(12, 0))) {
                        return DayPeriod.MORNING;
                      } else if (!time.isBefore(LocalTime.of(12, 0))
                          && time.isBefore(LocalTime.of(19, 0))) {
                        return DayPeriod.AFTERNOON;
                      } else {
                        return DayPeriod.EVENING;
                      }
                    },
                    Collectors.counting()));

    return Stream.of(DayPeriod.values())
        .map(
            period ->
                new ToiletPeriodCount(period, periodCountMap.getOrDefault(period, 0L).intValue()))
        .sorted(Comparator.comparingInt(ToiletPeriodCount::count).reversed())
        .toList();
  }
}
