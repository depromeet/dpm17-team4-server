package depromeet.lessonfour.server.report.app.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.report.domain.service.PooEvaluationService;
import depromeet.lessonfour.server.report.domain.vo.PooReport;
import depromeet.lessonfour.server.toiletrecord.app.service.ToiletRecordQueryService;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PooReportService {

  private final ToiletRecordQueryService toiletRecordQueryService;
  private final PooEvaluationService pooEvaluationService;

  public PooReport generateDailyReport(Long userId, LocalDateTime baseDateTime) {
    List<ToiletRecord> dailyRecords =
        toiletRecordQueryService.findByDate(userId, baseDateTime.toLocalDate());

    return pooEvaluationService.summarize(dailyRecords);
  }
}
