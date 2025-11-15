package depromeet.lessonfour.server.report.domain.vo.toilet;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import depromeet.lessonfour.server.common.api.code.ErrorCode;
import depromeet.lessonfour.server.common.exception.ServerException;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import lombok.Getter;

@Getter
public class WeeklyToiletReport {

  /** 해당 주(월~일)의 DailyToiletReport 리스트 */
  private final List<DailyToiletReport> dailyReports = new ArrayList<>();

  public static WeeklyToiletReport summarize(List<ToiletRecord> records) {
    WeeklyToiletReport weeklyReport = new WeeklyToiletReport();
    Map<LocalDate, List<ToiletRecord>> recordsByDate =
        records.stream().collect(Collectors.groupingBy(r -> r.getActivityAt().toDate()));

    List<DailyToiletReport> dailyReports =
        recordsByDate.entrySet().stream()
            .sorted(Entry.comparingByKey()) // 날짜 오름차순
            .map(entry -> ToiletEvaluation.summarize(entry.getValue()))
            .toList();
    weeklyReport.dailyReports.addAll(dailyReports);

    hasMoreThanOneDailyReport(dailyReports);

    return weeklyReport;
  }

  private static void hasMoreThanOneDailyReport(List<DailyToiletReport> dailyReports) {
    if (dailyReports.size() < 2) {
      throw new ServerException(ErrorCode.INSUFFICIENT_DATA_FOR_REPORT);
    }
  }

  public int size() {
    return dailyReports.size();
  }

  /** 주간 평균 배변 점수 */
  public double getAverageScore() {
    return dailyReports.stream().mapToDouble(DailyToiletReport::getToiletScore).average().orElse(0);
  }

  public ToiletEvaluationLevel getLevel() {
    return ToiletEvaluationLevel.from(getAverageScore());
  }

  /** 일별 배변 점수 리스트 */
  public List<Double> getScores() {
    return dailyReports.stream()
        .map(DailyToiletReport::getToiletScore)
        .collect(Collectors.toList());
  }
}
