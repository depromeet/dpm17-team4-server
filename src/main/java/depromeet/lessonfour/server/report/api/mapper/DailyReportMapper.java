package depromeet.lessonfour.server.report.api.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.report.api.dto.response.GetDailyReportResponseDto;
import depromeet.lessonfour.server.report.api.mapper.activity.FoodMapper;
import depromeet.lessonfour.server.report.api.mapper.activity.StressMapper;
import depromeet.lessonfour.server.report.api.mapper.activity.WaterMapper;
import depromeet.lessonfour.server.report.app.dto.response.DailyReport;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DailyReportMapper {

  private final ToiletReportMapper toiletReportMapper;
  private final FoodMapper foodMapper;
  private final WaterMapper waterMapper;
  private final StressMapper stressMapper;

  public GetDailyReportResponseDto map(DailyReport dailyReport, LocalDateTime updatedAt) {
    return new GetDailyReportResponseDto(
        updatedAt,
        toiletReportMapper.mapDaily(dailyReport.dailyToiletReport()),
        foodMapper.mapDaily(dailyReport.dailyActivityReport().getFoodEvaluations()),
        waterMapper.mapDaily(dailyReport.dailyActivityReport().getWaterEvaluations()),
        stressMapper.mapDaily(dailyReport.dailyActivityReport().getStressEvaluation()));
  }
}
