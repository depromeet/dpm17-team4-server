package depromeet.lessonfour.server.report.domain.service;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.report.domain.policy.ToiletEvaluationPolicy;
import depromeet.lessonfour.server.report.domain.vo.DailyToiletReport;
import depromeet.lessonfour.server.report.domain.vo.ToiletEvaluation;
import depromeet.lessonfour.server.report.domain.vo.monthly.DayPeriod;
import depromeet.lessonfour.server.report.domain.vo.monthly.ToiletPainDistribution;
import depromeet.lessonfour.server.report.domain.vo.monthly.ToiletPeriodCount;
import depromeet.lessonfour.server.report.domain.vo.monthly.ToiletTimeDistribution;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ToiletEvaluationService {

  private final ToiletEvaluationPolicy toiletEvaluationPolicy;

  public DailyToiletReport summarize(List<ToiletRecord> records) {
    List<ToiletEvaluation> evaluations =
        records.stream().map(toiletEvaluationPolicy::evaluate).toList();

    return DailyToiletReport.summarize(evaluations);
  }

  public ToiletTimeDistribution getTimeDistributions(List<ToiletRecord> toiletRecords) {
    Map<String, Long> timeGroups =
        toiletRecords.stream()
            .collect(
                Collectors.groupingBy(
                    record -> {
                      int duration = record.getDuration();
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

  public ToiletPainDistribution getPainDistribution(
      List<ToiletRecord> currentMonth, List<ToiletRecord> lastMonth) {
    int veryLow = (int) currentMonth.stream().filter(r -> r.getPain() < 10).count();
    int low =
        (int) currentMonth.stream().filter(r -> r.getPain() >= 10 && r.getPain() < 30).count();
    int medium =
        (int) currentMonth.stream().filter(r -> r.getPain() >= 30 && r.getPain() < 50).count();
    int high =
        (int) currentMonth.stream().filter(r -> r.getPain() >= 50 && r.getPain() < 70).count();
    int veryHigh = (int) currentMonth.stream().filter(r -> r.getPain() >= 70).count();

    // 지난 달의 아팠던 횟수 계산
    int lastMonthPainCount = (int) lastMonth.stream().filter(r -> r.getPain() >= 50).count();

    // 이번 달의 아팠던 횟수 계산
    int currentMonthPainCount = (int) currentMonth.stream().filter(r -> r.getPain() >= 50).count();

    int painDiff = currentMonthPainCount - lastMonthPainCount;

    return new ToiletPainDistribution(veryLow, low, medium, high, veryHigh, painDiff);
  }

  public List<ToiletPeriodCount> getPeriodDistribution(List<ToiletRecord> toiletRecords) {
    Map<DayPeriod, Long> periodCountMap =
        toiletRecords.stream()
            .collect(
                Collectors.groupingBy(
                    record -> {
                      LocalTime time = record.getActivityAt().getTime();

                      if (!time.isBefore(LocalTime.of(5, 0))
                          && time.isBefore(LocalTime.of(12, 0))) {
                        return DayPeriod.MORNING;
                      } else if (!time.isBefore(LocalTime.of(12, 0))
                          && time.isBefore(LocalTime.of(18, 0))) {
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
        .toList();
  }
}
