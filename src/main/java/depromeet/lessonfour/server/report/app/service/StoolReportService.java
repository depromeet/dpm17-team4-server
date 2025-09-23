package depromeet.lessonfour.server.report.app.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.report.domain.service.StoolEvaluationService;
import depromeet.lessonfour.server.report.domain.vo.StoolReport;
import depromeet.lessonfour.server.toiletrecord.app.service.ToiletRecordQueryService;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoolReportService {

  private final ToiletRecordQueryService toiletRecordQueryService;
  private final StoolEvaluationService stoolEvaluationService;

  public StoolReport generateDailyReport(Long userId, LocalDateTime baseDateTime) {
    List<ToiletRecord> dailyRecords =
        toiletRecordQueryService.findByDate(userId, baseDateTime.toLocalDate());

    return stoolEvaluationService.summarize(dailyRecords);
  }
}
