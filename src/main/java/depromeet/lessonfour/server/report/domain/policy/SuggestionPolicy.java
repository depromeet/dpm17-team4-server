package depromeet.lessonfour.server.report.domain.policy;

import static depromeet.lessonfour.server.report.domain.vo.Suggestion.ToiletSuggestion.BLOODY_STOOL;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.report.domain.vo.DailyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.DailyToiletReport;
import depromeet.lessonfour.server.report.domain.vo.DayType;
import depromeet.lessonfour.server.report.domain.vo.Suggestion;
import depromeet.lessonfour.server.report.domain.vo.Suggestion.HabitSuggestion;
import depromeet.lessonfour.server.report.domain.vo.Suggestion.ToiletSuggestion;
import depromeet.lessonfour.server.report.domain.vo.Suggestion.WaterSuggestion;
import depromeet.lessonfour.server.report.domain.vo.ToiletEvaluationLevel;
import depromeet.lessonfour.server.report.domain.vo.WaterEvaluation;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletShape;

@Component
public class SuggestionPolicy {

  private static final int VERY_GOOD_CONDITION_THRESHOLD = 80;
  private static final int CONSTIPATION_THRESHOLD = 2;
  private static final int PAINFUL_THRESHOLD = 70;
  private static final int PAIN_MILD_THRESHOLD = 20;
  private static final int NORMAL_DURATION_THRESHOLD = 10;
  private static final int SHORT_DURATION_THRESHOLD = 3;

  public Suggestion evaluate(
      DailyActivityReport dailyActivityReport, DailyToiletReport dailyToiletReport) {
    WaterSuggestion waterSuggestion = generateWaterSuggestions(dailyActivityReport);
    ToiletSuggestion toiletSuggestion = generatePooSuggestions(dailyToiletReport);
    List<HabitSuggestion> habitSuggestions =
        generateHabitSuggestions(dailyActivityReport, dailyToiletReport);

    return new Suggestion(waterSuggestion, toiletSuggestion, habitSuggestions);
  }

  private WaterSuggestion generateWaterSuggestions(DailyActivityReport dailyActivityReport) {
    WaterEvaluation water =
        dailyActivityReport.getWaterEvaluations().stream()
            .filter(w -> w.getDayType() == DayType.TODAY)
            .findFirst()
            .orElse(WaterEvaluation.empty());

    return switch (water.getLevel()) {
      case HIGH -> WaterSuggestion.HIGH;
      case MEDIUM -> WaterSuggestion.MEDIUM;
      case LOW -> WaterSuggestion.LOW;
      case NONE -> WaterSuggestion.NONE;
    };
  }

  private boolean hasOnlyPositiveHabits(List<HabitSuggestion> suggestions) {
    return suggestions.stream().allMatch(HabitSuggestion::isPositive);
  }

  private void addPositiveReinforcementSuggestions(
      List<HabitSuggestion> result,
      DailyActivityReport dailyActivityReport,
      DailyToiletReport dailyToiletReport) {

    if (dailyActivityReport.isStressWellManaged()) {
      result.add(HabitSuggestion.STRESS_MANAGEMENT);
    }

    if (dailyToiletReport.getToiletScore() >= VERY_GOOD_CONDITION_THRESHOLD) {
      result.add(HabitSuggestion.REGULAR_TOILET_HABITS);
    }
  }

  private ToiletSuggestion generatePooSuggestions(DailyToiletReport dailyToiletReport) {

    ToiletShape shape = dailyToiletReport.getMostFrequentShape().orElse(null);

    // 가장 심각한 증상부터 체크
    if (dailyToiletReport.hasBlood()) {
      return BLOODY_STOOL;
    }

    if (dailyToiletReport.hasAbnormalColor()) {
      return ToiletSuggestion.COLOR_ABNORMAL;
    }

    // 배변 시 통증이 있는 경우
    if (dailyToiletReport.getAveragePain() >= PAINFUL_THRESHOLD) {
      return ToiletSuggestion.PAINFUL_DEFECATION;
    }

    // 변비 판정 (딱딱하고 배변 횟수가 적음)
    if (shape == ToiletShape.ROCK
        && dailyToiletReport.getNumberOfRecords() < CONSTIPATION_THRESHOLD) {
      return ToiletSuggestion.CONSTIPATION;
    }

    // 설사 판정 (묽고 잦음)
    if (shape == ToiletShape.PORRIDGE
        && dailyToiletReport.getNumberOfRecords() > CONSTIPATION_THRESHOLD) {
      return ToiletSuggestion.DIARRHEA;
    }

    // 딱딱한 변
    if (shape == ToiletShape.ROCK) {
      return ToiletSuggestion.HARD_STOOL;
    }

    // 묽은 변
    if (shape == ToiletShape.PORRIDGE || shape == ToiletShape.CREAM) {
      return ToiletSuggestion.SOFT_STOOL;
    }

    // 배변 시간이 긴 경우
    if (dailyToiletReport.getAverageDuration() > NORMAL_DURATION_THRESHOLD) {
      return ToiletSuggestion.LONG_DEFECATION_TIME;
    }

    // 정상적인 경우들
    if ((dailyToiletReport.getLevel() == ToiletEvaluationLevel.GOOD
            || dailyToiletReport.getLevel() == ToiletEvaluationLevel.VERY_GOOD)
        && dailyToiletReport.getAverageDuration() <= NORMAL_DURATION_THRESHOLD
        && dailyToiletReport.getAveragePain() <= PAINFUL_THRESHOLD) {
      if (dailyToiletReport.hasGoodShape()) {
        return ToiletSuggestion.IDEAL_SHAPE;
      }

      if (dailyToiletReport.hasGoodColor()) {
        return ToiletSuggestion.HEALTHY_COLOR;
      }

      return ToiletSuggestion.HEALTHY_REGULAR;
    }

    // 통증 없는 배변
    if (dailyToiletReport.getAveragePain() <= PAIN_MILD_THRESHOLD) {
      return ToiletSuggestion.PAIN_FREE;
    }

    // 적절한 시간 내 배변
    if (dailyToiletReport.getAverageDuration() <= NORMAL_DURATION_THRESHOLD) {
      return ToiletSuggestion.NORMAL_DURATION;
    }

    // 기본값 (정상적인 규칙적 배변)
    return ToiletSuggestion.HEALTHY_REGULAR;
  }

  private List<HabitSuggestion> generateHabitSuggestions(
      DailyActivityReport dailyActivityReport, DailyToiletReport dailyToiletReport) {

    List<HabitSuggestion> result = new ArrayList<>();

    // 스트레스 레벨 평가
    if (dailyActivityReport.hasStress()) {
      result.add(HabitSuggestion.HIGH_STRESS_LEVEL);
    } else if (dailyActivityReport.isStressWellManaged()) {
      result.add(HabitSuggestion.STRESS_MANAGEMENT);
    }

    // 알코올 섭취 평가
    if (dailyToiletReport.drunkAlcohol()) {
      result.add(HabitSuggestion.EXCESSIVE_ALCOHOL);
    }

    // 화장실 사용 습관 평가
    if (dailyToiletReport.getAverageDuration() > NORMAL_DURATION_THRESHOLD) {
      result.add(HabitSuggestion.LONG_TOILET_TIME);
    } else if (dailyToiletReport.getAverageDuration() <= SHORT_DURATION_THRESHOLD) {
      result.add(HabitSuggestion.SHORT_TOILET_TIME);
    }

    // 8. 긍정적 습관 강화 (이미 좋은 습관이 있는 경우)
    if (result.isEmpty() || hasOnlyPositiveHabits(result)) {
      addPositiveReinforcementSuggestions(result, dailyActivityReport, dailyToiletReport);
    }

    return result;
  }
}
