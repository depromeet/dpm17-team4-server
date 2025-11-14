package depromeet.lessonfour.server.report.api.mapper.activity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.report.api.dto.response.GetDailyReportResponseDto.DailyStressReport;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyStressSection;
import depromeet.lessonfour.server.report.api.dto.response.GetMonthlyReportResponseDto.MonthlyStressSection.StressItem;
import depromeet.lessonfour.server.report.api.dto.response.GetWeeklyReportResponseDto.WeeklyStressSection;
import depromeet.lessonfour.server.report.domain.vo.MonthlyReport;
import depromeet.lessonfour.server.report.domain.vo.activity.DailyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.activity.StressEvaluation;
import depromeet.lessonfour.server.report.domain.vo.activity.WeeklyActivityReport;

@Component
public class StressMapper {

  private record StressMapping(String message, String image) {}

  private static final Map<StressEvaluation, StressMapping> STRESS_MAPPINGS =
      Map.of(
          StressEvaluation.VERY_HIGH,
              new StressMapping(
                  "삐용삐용! 스트레스 만땅! 산책이나 명상을 해볼까요?",
                  "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/stress/very_bad.png"),
          StressEvaluation.HIGH,
              new StressMapping(
                  "스트레스에 잡아먹히지 않도록 조심해봐요!",
                  "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/stress/bad.png"),
          StressEvaluation.MEDIUM,
              new StressMapping(
                  "스트레스 발생! 마음을 다스릴 수 있도록 노력해요",
                  "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/stress/normal.png"),
          StressEvaluation.LOW,
              new StressMapping(
                  "긍정적인 당신! 그 마인드 오래도록 유지해봐요",
                  "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/stress/good.png"),
          StressEvaluation.VERY_LOW,
              new StressMapping(
                  "스트레스 Zero! 행복한 하루가 되셨군요?",
                  "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/stress/very_good.png"));

  /** 일간 스트레스 리포트 매핑 */
  public DailyStressReport mapDaily(StressEvaluation stressEvaluation) {
    if (stressEvaluation == null || stressEvaluation == StressEvaluation.NONE) {
      return null;
    }

    StressMapping mapped = STRESS_MAPPINGS.get(stressEvaluation);
    if (mapped == null) {
      return null;
    }
    return new DailyStressReport(mapped.message, mapped.image);
  }

  /** 주간 스트레스 리포트 매핑 */
  public WeeklyStressSection mapWeekly(WeeklyActivityReport thisWeek) {
    // 1) 일단 items 리스트 만들고
    List<WeeklyStressSection.StressItem> items = new ArrayList<>();

    // 2) 아예 주간 활동이 없는 경우 (null or empty) → 기본 메시지 + 빈 리스트
    if (thisWeek.getDailyReports().isEmpty()) {

      return new WeeklyStressSection(
          "스트레스 기록이 없어요\n오늘부터 간단히 남겨봐요!",
          "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/condition_medium.png",
          items);
    }

    // 3) 주간 최악 스트레스 레벨 계산
    StressEvaluation worst =
        thisWeek.getDailyReports().stream()
            .map(DailyActivityReport::getStressEvaluation)
            .filter(s -> s != null && s != StressEvaluation.NONE)
            // NOTE: enum 선언이 VERY_LOW(최악) → ... → VERY_HIGH(최상), NONE(제외)
            // 낮을수록(ordinal 작을수록) 더 나쁜 상태이므로 min이 "최악"을 의미한다.
            .min(Comparator.comparingInt(Enum::ordinal))
            .orElse(StressEvaluation.NONE);

    // 4) 기존 StressMapper 사용하되, null 이면 기본값으로 보정
    DailyStressReport base = mapDaily(worst);

    if (base == null) {
      // NONE 등에 대해 StressMapper가 null을 주는 경우를 위한 안전장치
      base =
          new DailyStressReport(
              "스트레스 기록이 없어요\n오늘부터 간단히 남겨봐요!",
              "https://kr.object.ncloudstorage.com/depromeet-dev-static-resources/condition_medium.png");
    }

    // 5) 일별 아이템 생성 (요일 라벨은 원하는 대로)
    String[] dayNames = {
      "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"
    };

    int idx = 0;
    for (DailyActivityReport daily : thisWeek.getDailyReports()) {
      String day = idx < dayNames.length ? dayNames[idx] : "DAY_" + (idx + 1);
      StressEvaluation ev = daily.getStressEvaluation();
      String label = (ev == null ? StressEvaluation.NONE : ev).name(); // NULL-SAFE
      items.add(new WeeklyStressSection.StressItem(day, label));
      idx++;
    }

    // 6) 최종 섹션 반환
    return new WeeklyStressSection(base.message(), base.image(), items);
  }

  /** 월간 스트레스 리포트 매핑 */
  public MonthlyStressSection mapMonthly(MonthlyReport report) {
    StressEvaluation monthlyAverageStress = report.getMonthlyAverageStress();

    if (monthlyAverageStress == StressEvaluation.NONE) {
      return MonthlyStressSection.empty();
    }

    // 월 평균 스트레스 기준으로 메시지 생성
    StressMapping mapped = STRESS_MAPPINGS.get(monthlyAverageStress);

    // 주차별 평균 스트레스를 StressItem으로 매핑
    List<StressItem> items =
        report.getWeeklyAverageStress().stream()
            .map(
                weeklyAvg ->
                    new MonthlyStressSection.StressItem(
                        weeklyAvg.weekIndex() + "주차", weeklyAvg.representative().name()))
            .toList();

    return new MonthlyStressSection(mapped.message(), mapped.image(), items);
  }
}
