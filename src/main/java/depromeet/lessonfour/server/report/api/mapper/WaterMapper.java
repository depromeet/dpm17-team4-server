package depromeet.lessonfour.server.report.api.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.report.app.dto.response.GetDailyReportResponseDto.WaterReport;
import depromeet.lessonfour.server.report.app.dto.response.GetDailyReportResponseDto.WaterReportItem;
import depromeet.lessonfour.server.report.domain.vo.DayType;
import depromeet.lessonfour.server.report.domain.vo.Suggestion.WaterSuggestion;
import depromeet.lessonfour.server.report.domain.vo.WaterEvaluation;
import depromeet.lessonfour.server.report.domain.vo.WaterLevel;

@Component
public class WaterMapper {

  public WaterReport map(List<WaterEvaluation> waterEvaluations) {
    if (waterEvaluations == null || waterEvaluations.isEmpty()) {
      return null;
    }
    WaterEvaluation today = getEvaluationByDay(waterEvaluations, DayType.TODAY);
    WaterEvaluation yesterday = getEvaluationByDay(waterEvaluations, DayType.YESTERDAY);

    String message = getMessage(today);

    return new WaterReport(
        message,
        List.of(
            mapItem("STANDARD", 2000.0, null),
            mapItem("YESTERDAY", yesterday != null ? yesterday.getQuantity() : 0.0, yesterday),
            mapItem("TODAY", today != null ? today.getQuantity() : 0.0, today)));
  }

  private static String getMessage(WaterEvaluation evaluation) {
    if (evaluation == null) {
      return "물 섭취 기록이 없어요\n물을 자주 마셔주세요";
    }
    WaterLevel waterLevel = evaluation.getLevel();
    return switch (waterLevel) {
      case HIGH, MEDIUM -> "물을 잘 섭취하고 계시군요!\n앞으로도 잘 유지해봐요";
      case LOW -> "장이 말라가고 있어요!\n물 섭취량을 늘려야 해요";
      case NONE -> "물 섭취 기록이 없어요\n물을 자주 마셔주세요";
    };
  }

  private static WaterReportItem mapItem(String name, double value, WaterEvaluation evaluation) {
    if (evaluation == null) {
      return new WaterReportItem(
          name, value, WaterSuggestion.NONE.getColor(), WaterSuggestion.NONE);
    }
    WaterSuggestion suggestion = WaterSuggestion.from(evaluation.getLevel());
    return new WaterReportItem(name, value, suggestion.getColor(), suggestion);
  }

  private static WaterEvaluation getEvaluationByDay(
      List<WaterEvaluation> evaluations, DayType dayType) {
    return evaluations.stream()
        .filter(evaluation -> evaluation.getDayType() == dayType)
        .findFirst()
        .orElse(null);
  }
}
