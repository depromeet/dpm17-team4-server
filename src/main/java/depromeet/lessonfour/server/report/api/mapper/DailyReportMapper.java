package depromeet.lessonfour.server.report.api.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.report.app.dto.response.DailyReport;
import depromeet.lessonfour.server.report.app.dto.response.GetDailyReportResponseDto;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DailyReportMapper {

  private final StoolMapper stoolMapper;
  private final FoodMapper foodMapper;
  private final WaterMapper waterMapper;
  private final StressMapper stressMapper;
  private final SuggestionMapper suggestionMapper;

  public GetDailyReportResponseDto map(DailyReport dailyReport, LocalDateTime updatedAt) {
    return new GetDailyReportResponseDto(
        updatedAt,
        stoolMapper.map(dailyReport.stoolReport()),
        foodMapper.map(dailyReport.activityReport().getFoodEvaluations()),
        waterMapper.map(dailyReport.activityReport().getWaterEvaluations()),
        stressMapper.map(dailyReport.activityReport().getStressEvaluation()),
        suggestionMapper.map(dailyReport.suggestion()));
  }
}
