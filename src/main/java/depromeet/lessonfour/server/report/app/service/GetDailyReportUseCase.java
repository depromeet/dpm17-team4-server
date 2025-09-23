package depromeet.lessonfour.server.report.app.service;

import depromeet.lessonfour.server.activityrecord.app.service.ActivityRecordQueryService;
import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.report.app.dto.response.GetDailyReportResponseDto;
import depromeet.lessonfour.server.report.app.support.DailyReportMapper;
import depromeet.lessonfour.server.toiletrecord.app.service.ToiletRecordQueryService;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetDailyReportUseCase {

  private final DailyReportMapper dailyReportMapper;
  private final ActivityRecordQueryService activityRecordQueryService;
  private final ToiletRecordQueryService toiletRecordQueryService;

  public GetDailyReportResponseDto getDailyReport(Long userId, LocalDateTime dateTime) {
    LocalDate date = dateTime.toLocalDate();
    LocalDate dayBefore = date.minusDays(1);
    List<ActivityRecord> activityRecords = activityRecordQueryService.findByUserAndPeriod(userId,
        date, dayBefore);
    ToiletRecord toiletRecord = toiletRecordQueryService.findByUserIdAndActivityAt(userId,
        dateTime);


  }
}
