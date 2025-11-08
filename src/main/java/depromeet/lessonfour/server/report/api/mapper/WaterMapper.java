package depromeet.lessonfour.server.report.api.mapper;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.report.api.dto.response.GetDailyReportResponseDto.WaterReport;
import depromeet.lessonfour.server.report.api.dto.response.GetDailyReportResponseDto.WaterReportItem;
import depromeet.lessonfour.server.report.domain.vo.DayType;
import depromeet.lessonfour.server.report.domain.vo.Suggestion.WaterSuggestion;
import depromeet.lessonfour.server.report.domain.vo.WaterEvaluation;
import depromeet.lessonfour.server.report.domain.vo.WaterLevel;

@Component
public class WaterMapper {

  static Map<WaterSuggestion, String> COLOR_MAP =
      Map.of(
          WaterSuggestion.STANDARD, "#4E5560",
          WaterSuggestion.HIGH, "#23ABFF",
          WaterSuggestion.MEDIUM, "#F4B005",
          WaterSuggestion.LOW, "#F13A49",
          WaterSuggestion.NONE, "#D9D9D9");

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
            mapItem("YESTERDAY", yesterday.getQuantity() * 200, yesterday),
            mapItem("TODAY", today.getQuantity() * 200, today)));
  }

  private static String getMessage(WaterEvaluation evaluation) {
    if (evaluation == null) {
      return "물 섭취 기록이 없어요. 물을 자주 마셔주세요";
    }
    WaterLevel waterLevel = evaluation.getLevel();
    return switch (waterLevel) {
      case HIGH -> "훌륭해요! 물 섭취 만점입니다. 앞으로도 잘 유지해봐요";
      case MEDIUM -> "보통 수준이에요. 조금 더 자주 물을 마셔보세요!";
      case LOW -> "장이 말라가고 있어요! 물 섭취량을 늘려야 해요";
      case NONE -> "물 섭취 기록이 없어요. 물을 자주 마셔주세요";
    };
  }

  private static WaterReportItem mapItem(String name, double value, WaterEvaluation evaluation) {
    if (evaluation == null) {
      // STANDARD 항목인 경우
      return new WaterReportItem(
          name, value, COLOR_MAP.get(WaterSuggestion.NONE), WaterSuggestion.NONE);
    }

    WaterSuggestion suggestion = WaterSuggestion.from(evaluation.getLevel());
    return new WaterReportItem(name, value, COLOR_MAP.get(suggestion), suggestion);
  }

  private static WaterEvaluation getEvaluationByDay(
      List<WaterEvaluation> evaluations, DayType dayType) {
    return evaluations.stream()
        .filter(evaluation -> evaluation.getDayType() == dayType)
        .findFirst()
        .orElseGet(() -> WaterEvaluation.empty(dayType));
  }
}
