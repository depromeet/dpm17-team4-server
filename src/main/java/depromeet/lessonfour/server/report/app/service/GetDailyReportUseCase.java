package depromeet.lessonfour.server.report.app.service;

import java.time.LocalDateTime;

import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.report.app.dto.response.GetDailyReportResponseDto;
import depromeet.lessonfour.server.report.app.mapper.DailyReportMapper;
import depromeet.lessonfour.server.report.domain.service.DailyFoodEvaluator;
import depromeet.lessonfour.server.report.domain.service.DailyPooEvaluator;
import depromeet.lessonfour.server.report.domain.service.DailyStressEvaluator;
import depromeet.lessonfour.server.report.domain.service.DailySuggestionEvaluator;
import depromeet.lessonfour.server.report.domain.service.DailyWaterEvaluator;
import depromeet.lessonfour.server.report.domain.vo.FoodEvaluation;
import depromeet.lessonfour.server.report.domain.vo.PooEvaluation;
import depromeet.lessonfour.server.report.domain.vo.StressEvaluation;
import depromeet.lessonfour.server.report.domain.vo.SuggestionEvaluation;
import depromeet.lessonfour.server.report.domain.vo.WaterEvaluation;
import lombok.RequiredArgsConstructor;

@UseCase
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetDailyReportUseCase {

  private final DailyFoodEvaluator dailyFoodEvaluator;
  private final DailyPooEvaluator dailyPooEvaluator;
  private final DailyStressEvaluator dailyStressEvaluator;
  private final DailyWaterEvaluator dailyWaterEvaluator;
  private final DailySuggestionEvaluator dailySuggestionEvaluator;
  private final DailyReportMapper dailyReportMapper;

  public GetDailyReportResponseDto getDailyReport(Long userId, LocalDateTime dataTime) {
    FoodEvaluation food = dailyFoodEvaluator.evaluate();
    PooEvaluation poo = dailyPooEvaluator.evaluate();
    StressEvaluation stress = dailyStressEvaluator.evaluate();
    WaterEvaluation water = dailyWaterEvaluator.evaluate();
    SuggestionEvaluation suggestion = dailySuggestionEvaluator.evaluate();

    return dailyReportMapper.toResponse(LocalDateTime.now(), food, poo, stress, water, suggestion);
  }
}
