package depromeet.lessonfour.server.report.app.service;

import java.time.YearMonth;

import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.report.app.dto.response.MonthlyReport;
import depromeet.lessonfour.server.report.app.dto.response.RecordCounts;
import depromeet.lessonfour.server.report.domain.vo.monthly.MonthlyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.monthly.MonthlyToiletReport;
import lombok.RequiredArgsConstructor;

@UseCase
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MonthlyReportUseCase {

  ToiletReportService toiletReportService;
  ActivityReportService activityReportService;

  public MonthlyReport getMonthlyReport(Long userId, YearMonth month) {
    ActivityAt monthStart = ActivityAt.of(month.atDay(1));
    ActivityAt monthEndExclusive = ActivityAt.of(month.atEndOfMonth().plusDays(1));

    MonthlyToiletReport toiletReport =
        toiletReportService.generateMonthlyReport(userId, monthStart, monthEndExclusive);

    MonthlyActivityReport activityReport =
        activityReportService.generateMonthlyReport(userId, monthStart, monthEndExclusive);

    // 기록수
    RecordCounts recordCounts = RecordCounts.of(activityReport.size(), toiletReport.size());
  }
}
