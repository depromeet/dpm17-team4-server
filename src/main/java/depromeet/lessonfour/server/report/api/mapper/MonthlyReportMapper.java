package depromeet.lessonfour.server.report.api.mapper;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyRecordCount;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyToiletTime;
import depromeet.lessonfour.server.report.app.dto.response.MonthlyReport;
import depromeet.lessonfour.server.report.app.dto.response.RecordCounts;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MonthlyReportMapper {

  private final ToiletReportMapper toiletReportMapper;

  public GetMonthlyReportResponseDto map(MonthlyReport monthlyReport) {

    return new GetMonthlyReportResponseDto(
        from(monthlyReport.recordCounts()),
        toiletReportMapper.mapShape(monthlyReport.shape()),
        MonthlyToiletTime.from(monthlyReport.timeDistribution()),
        toiletReportMapper.mapColor(monthlyReport.color()),
        toiletReportMapper.mapPain(monthlyReport.pain()),
        toiletReportMapper.mapPeriod(monthlyReport.timeOfDay()));
  }

  private static MonthlyRecordCount from(RecordCounts recordCounts) {
    return new MonthlyRecordCount(
        recordCounts.totalCount(), recordCounts.toiletCount(), recordCounts.activityCount());
  }
}
