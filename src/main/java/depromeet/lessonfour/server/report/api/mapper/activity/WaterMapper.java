package depromeet.lessonfour.server.report.api.mapper.activity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.report.api.dto.response.GetDailyReportResponseDto.DailyWaterReport;
import depromeet.lessonfour.server.report.api.dto.response.GetDailyReportResponseDto.WaterReportItem;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyWaterSection;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyWaterSection.WaterItem;
import depromeet.lessonfour.server.report.app.dto.response.MonthlyReport;
import depromeet.lessonfour.server.report.domain.vo.activity.DailyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.activity.DayType;
import depromeet.lessonfour.server.report.domain.vo.activity.MonthlyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.activity.WaterEvaluation;
import depromeet.lessonfour.server.report.domain.vo.activity.WaterLevel;
import depromeet.lessonfour.server.report.domain.vo.activity.WeeklyActivityReportGroup;

@Component
public class WaterMapper {

  private static final String STANDARD_BAR_COLOR = "#4E5560";
  private static final String WATER_BAR_COLOR = "#7850FB";

  /** 일간 물 보고서 매핑 */
  public DailyWaterReport mapDaily(List<WaterEvaluation> waterEvaluations) {
    if (waterEvaluations == null || waterEvaluations.isEmpty()) {
      return null;
    }
    WaterEvaluation today = getEvaluationByDay(waterEvaluations, DayType.TODAY);
    WaterEvaluation yesterday = getEvaluationByDay(waterEvaluations, DayType.YESTERDAY);

    String message = getMessage(today);

    return new DailyWaterReport(
        message,
        List.of(
            new WaterReportItem("STANDARD", 2000.0, STANDARD_BAR_COLOR, WaterLevel.NONE),
            new WaterReportItem(
                "YESTERDAY", yesterday.getQuantity() * 200, WATER_BAR_COLOR, yesterday.getLevel()),
            new WaterReportItem(
                "TODAY", today.getQuantity() * 200, WATER_BAR_COLOR, today.getLevel())));
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

  private static WaterEvaluation getEvaluationByDay(
      List<WaterEvaluation> evaluations, DayType dayType) {
    return evaluations.stream()
        .filter(evaluation -> evaluation.getDayType() == dayType)
        .findFirst()
        .orElseGet(() -> WaterEvaluation.empty(dayType));
  }

  /** 월간 물 보고서 매핑 */
  public MonthlyWaterSection mapMonthly(MonthlyReport report) {
    MonthlyActivityReport activityReport = report.activityReport();
    if (activityReport.weeklyActivityReportGroups().isEmpty()) {
      return MonthlyWaterSection.empty();
    }

    List<WaterItem> items = new ArrayList<>();
    double highValue = 2000.0, mediumValue = 1200.0, lowValue = 600.0;

    List<WaterLevel> weekLevels = new ArrayList<>();
    for (WeeklyActivityReportGroup g : activityReport.weeklyActivityReportGroups()) {
      WaterLevel weekLevel = aggregateWeeklyWaterLevel(g); // 일별 레벨 평균(반올림)
      weekLevels.add(weekLevel);

      double value =
          switch (weekLevel) {
            case HIGH -> highValue;
            case MEDIUM -> mediumValue;
            case LOW -> lowValue;
            case NONE -> 0.0;
          };
      items.add(new WaterItem(g.weekIndex() + "주차", value));
    }

    // 월 레벨 = NONE 제외 주차 레벨들의 ordinal 평균(반올림)
    int sum = 0, cnt = 0;
    for (WaterLevel wl : weekLevels) {
      if (wl != WaterLevel.NONE) {
        sum += wl.ordinal();
        cnt++;
      }
    }
    WaterLevel monthLevel;
    if (cnt == 0) {
      monthLevel = WaterLevel.NONE;
    } else {
      int avgOrdinal = Math.round((float) sum / cnt);
      WaterLevel[] lvls = WaterLevel.values();
      // 안전 클램프
      avgOrdinal = Math.min(Math.max(avgOrdinal, 0), lvls.length - 1);
      monthLevel = lvls[avgOrdinal];
    }

    String message =
        switch (monthLevel) {
          case NONE -> "물 섭취 기록이 없어요\n물을 자주 마셔주세요";
          case LOW -> "장이 말라가고 있어요!\n물 섭취량을 늘려야 해요";
          case MEDIUM, HIGH -> "물을 잘 섭취하고 계시군요!\n앞으로도 잘 유지해봐요";
        };

    return new MonthlyWaterSection(message, items);
  }

  private static WaterLevel aggregateWeeklyWaterLevel(WeeklyActivityReportGroup g) {
    if (g.dailyReports().isEmpty()) {
      return WaterLevel.NONE;
    }

    int sumOrdinal = 0, cnt = 0;
    for (DailyActivityReport d : g.dailyReports()) {
      List<WaterEvaluation> ws = d.getWaterEvaluations();
      if (ws != null && !ws.isEmpty()) {
        WaterLevel lvl = ws.getFirst().getLevel(); // 동일 기준 유지
        sumOrdinal += lvl.ordinal();
        cnt++;
      }
    }
    if (cnt == 0) {
      return WaterLevel.NONE;
    }
    int avgOrdinal = Math.round((float) sumOrdinal / cnt);
    WaterLevel[] levels = WaterLevel.values();
    return levels[Math.min(Math.max(avgOrdinal, 0), levels.length - 1)];
  }
}
