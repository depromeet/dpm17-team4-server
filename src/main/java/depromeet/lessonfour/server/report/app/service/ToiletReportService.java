package depromeet.lessonfour.server.report.app.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.report.app.client.ToiletRecordClient;
import depromeet.lessonfour.server.report.domain.service.ToiletEvaluationService;
import depromeet.lessonfour.server.report.domain.vo.ToiletReport;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ToiletReportService {

  private final ToiletRecordClient toiletRecordClient;
  private final ToiletEvaluationService toiletEvaluationService;
  private final ToiletScoreService toiletScoreService;

  public ToiletReport generateDailyReport(Long userId, LocalDateTime baseDateTime) {
    List<ToiletRecord> dailyRecords =
        toiletRecordClient.getToiletRecordsByDate(userId, baseDateTime.toLocalDate());

    ToiletReport report = toiletEvaluationService.summarize(dailyRecords);
    toiletScoreService.updateScore(
        userId, (int) report.getToiletScore(), ActivityAt.from(baseDateTime));

    return report;
  }
}
