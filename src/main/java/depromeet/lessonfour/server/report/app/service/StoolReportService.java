package depromeet.lessonfour.server.report.app.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.report.app.client.ToiletRecordClient;
import depromeet.lessonfour.server.report.domain.service.StoolEvaluationService;
import depromeet.lessonfour.server.report.domain.vo.StoolReport;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoolReportService {

  private final ToiletRecordClient toiletRecordClient;
  private final StoolEvaluationService stoolEvaluationService;
  private final ToiletScoreService toiletScoreService;

  public StoolReport generateDailyReport(Long userId, LocalDateTime baseDateTime) {
    List<ToiletRecord> dailyRecords =
        toiletRecordClient.getToiletRecordsByDate(userId, baseDateTime.toLocalDate());

    StoolReport report = stoolEvaluationService.summarize(dailyRecords);
    toiletScoreService.updateScore(
        userId, (int) report.getStoolScore(), ActivityAt.from(baseDateTime));

    return report;
  }
}
