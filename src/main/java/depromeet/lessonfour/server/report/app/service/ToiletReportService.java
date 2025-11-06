package depromeet.lessonfour.server.report.app.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.report.app.client.ToiletRecordClient;
import depromeet.lessonfour.server.report.app.dto.response.ToiletColorCount;
import depromeet.lessonfour.server.report.app.dto.response.ToiletShapeCount;
import depromeet.lessonfour.server.report.domain.service.ToiletEvaluationService;
import depromeet.lessonfour.server.report.domain.vo.DailyToiletReport;
import depromeet.lessonfour.server.report.domain.vo.monthly.MonthlyToiletReport;
import depromeet.lessonfour.server.report.domain.vo.monthly.ScoreSummary;
import depromeet.lessonfour.server.report.domain.vo.monthly.ToiletPainDistribution;
import depromeet.lessonfour.server.report.domain.vo.monthly.ToiletPeriodCount;
import depromeet.lessonfour.server.report.domain.vo.monthly.ToiletTimeDistribution;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletColor;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletShape;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ToiletReportService {

  private final ToiletRecordClient toiletRecordClient;
  private final ToiletEvaluationService toiletEvaluationService;
  private final ToiletScoreService toiletScoreService;

  public DailyToiletReport generateDailyReport(Long userId, LocalDateTime baseDateTime) {
    List<ToiletRecord> dailyRecords =
        toiletRecordClient.getToiletRecordsByDate(userId, baseDateTime.toLocalDate());

    DailyToiletReport report = toiletEvaluationService.summarize(dailyRecords);
    toiletScoreService.updateScore(
        userId, (int) report.getToiletScore(), ActivityAt.from(baseDateTime));

    return report;
  }

  public MonthlyToiletReport generateMonthlyReport(
      Long userId, ActivityAt start, ActivityAt endExclusive) {
    ActivityAt lastMonthStart = start.getLastMonth();

    List<ToiletRecord> toiletRecords =
        toiletRecordClient.getToiletRecordsByActivityAtBetween(userId, start, endExclusive);
    List<ToiletRecord> lastMonthToiletRecords =
        toiletRecordClient.getToiletRecordsByActivityAtBetween(userId, lastMonthStart, start);

    // 리포트 횟수
    int toiletCount = toiletRecords.size();

    // 월간 기록 분석 (best, worst)
    ScoreSummary scoreSummary =
        toiletScoreService.getMonthlyScoreSummary(userId, start, endExclusive);

    // 모양
    List<ToiletShapeCount> mostShapes = getMostShape(toiletRecords);

    // 소요 시간
    ToiletTimeDistribution timeDistribution = getTimeDistributions(toiletRecords);

    // 색상
    List<ToiletColorCount> mostColors = getMostColor(toiletRecords);

    // 복통
    ToiletPainDistribution painDistribution =
        getPainDistribution(toiletRecords, lastMonthToiletRecords);

    // 배변 시간
    List<ToiletPeriodCount> periodCount = getPeriodDistribution(toiletRecords);

    return MonthlyToiletReport.of(
        toiletCount,
        scoreSummary,
        mostShapes,
        timeDistribution,
        mostColors,
        painDistribution,
        periodCount);
  }

  List<ToiletShapeCount> getMostShape(List<ToiletRecord> toiletRecords) {
    Map<ToiletShape, Long> shapeCountMap =
        toiletRecords.stream()
            .filter(record -> record.getShape() != null)
            .collect(Collectors.groupingBy(ToiletRecord::getShape, Collectors.counting()));

    return shapeCountMap.entrySet().stream()
        .sorted(Map.Entry.<ToiletShape, Long>comparingByValue().reversed())
        .limit(3)
        .map(entry -> new ToiletShapeCount(entry.getKey(), entry.getValue().intValue()))
        .toList();
  }

  List<ToiletColorCount> getMostColor(List<ToiletRecord> toiletRecords) {
    Map<ToiletColor, Long> colorCountMap =
        toiletRecords.stream()
            .filter(record -> record.getColor() != null) // NPE 방지: null color 필터링
            .collect(Collectors.groupingBy(ToiletRecord::getColor, Collectors.counting()));

    return colorCountMap.entrySet().stream()
        .sorted(Map.Entry.<ToiletColor, Long>comparingByValue().reversed())
        .limit(3)
        .map(entry -> new ToiletColorCount(entry.getKey(), entry.getValue().intValue()))
        .toList();
  }

  ToiletTimeDistribution getTimeDistributions(List<ToiletRecord> toiletRecords) {
    return toiletEvaluationService.getTimeDistributions(toiletRecords);
  }

  ToiletPainDistribution getPainDistribution(
      List<ToiletRecord> currentMonth, List<ToiletRecord> lastMonth) {
    return toiletEvaluationService.getPainDistribution(currentMonth, lastMonth);
  }

  List<ToiletPeriodCount> getPeriodDistribution(List<ToiletRecord> toiletRecords) {
    return toiletEvaluationService.getPeriodDistribution(toiletRecords);
  }
}
