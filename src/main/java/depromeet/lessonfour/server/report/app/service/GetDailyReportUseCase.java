package depromeet.lessonfour.server.report.app.service;

import java.time.LocalDateTime;

import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.report.app.dto.response.GetDailyReportResponseDto;
import depromeet.lessonfour.server.report.app.support.DailyReportMapper;
import depromeet.lessonfour.server.report.domain.vo.ActivityReport;
import depromeet.lessonfour.server.report.domain.vo.PooReport;
import lombok.RequiredArgsConstructor;

@UseCase
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetDailyReportUseCase {

  private final DailyReportMapper dailyReportMapper;
  private final ActivityReportService activityReportService;
  private final PooReportService pooReportService;

  public GetDailyReportResponseDto getDailyReport(Long userId, LocalDateTime dateTime) {
    ActivityReport activityReport = activityReportService.generateDailyReport(userId, dateTime);
    PooReport pooReport = pooReportService.generateDailyReport(userId, dateTime);

    return null;
  }
}
