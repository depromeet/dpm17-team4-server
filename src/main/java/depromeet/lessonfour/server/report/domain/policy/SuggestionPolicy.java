package depromeet.lessonfour.server.report.domain.policy;

import static depromeet.lessonfour.server.report.domain.vo.Suggestion.PooSuggestion.BLOODY_STOOL;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.report.domain.vo.ActivityReport;
import depromeet.lessonfour.server.report.domain.vo.PooEvaluationLevel;
import depromeet.lessonfour.server.report.domain.vo.PooReport;
import depromeet.lessonfour.server.report.domain.vo.Suggestion;
import depromeet.lessonfour.server.report.domain.vo.Suggestion.HabitSuggestion;
import depromeet.lessonfour.server.report.domain.vo.Suggestion.PooSuggestion;
import depromeet.lessonfour.server.report.domain.vo.Suggestion.WaterSuggestion;
import depromeet.lessonfour.server.report.domain.vo.WaterEvaluation;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletShape;

@Component
public class SuggestionPolicy {

  private static final int VERY_GOOD_CONDITION_THRESHOLD = 80;
  private static final int CONSTIPATION_THRESHOLD = 2;
  private static final int PAINFUL_THRESHOLD = 70;
  private static final int NORMAL_DURATION_THRESHOLD = 10;
  private static final int SHORT_DURATION_THRESHOLD = 3;

  public Suggestion suggest(ActivityReport activityReport, PooReport pooReport) {
    WaterSuggestion waterSuggestion = generateWaterSuggestions(activityReport);
    PooSuggestion pooSuggestion = generatePooSuggestions(pooReport);
    List<HabitSuggestion> habitSuggestions = generateHabitSuggestions(activityReport, pooReport);

    return new Suggestion(waterSuggestion, pooSuggestion, habitSuggestions);
  }

  private WaterSuggestion generateWaterSuggestions(ActivityReport activityReport) {
    WaterEvaluation water = activityReport.getWaterEvaluation();

    return switch (water.getLevel()) {
      case HIGH -> WaterSuggestion.HIGH;
      case MEDIUM -> WaterSuggestion.MEDIUM;
      case LOW, NONE -> WaterSuggestion.LOW;
    };
  }

  private boolean hasOnlyPositiveHabits(List<HabitSuggestion> suggestions) {
    return suggestions.stream().allMatch(HabitSuggestion::isPositive);
  }

  private void addPositiveReinforcementSuggestions(
      List<HabitSuggestion> result, ActivityReport activityReport, PooReport pooReport) {

    if (activityReport.isStressWellManaged()) {
      result.add(HabitSuggestion.STRESS_MANAGEMENT);
    }

    if (pooReport.getStoolScore() >= VERY_GOOD_CONDITION_THRESHOLD) {
      result.add(HabitSuggestion.REGULAR_TOILET_HABITS);
    }
  }

  private PooSuggestion generatePooSuggestions(PooReport pooReport) {

    ToiletShape shape = pooReport.getMostFrequentShape();
    // 가장 심각한 증상부터 체크
    if (pooReport.hasBlood()) {
      return BLOODY_STOOL;
    }

    if (pooReport.hasAbnormalColor()) {
      return PooSuggestion.COLOR_ABNORMAL;
    }

    // 배변 시 통증이 있는 경우
    if (pooReport.getAveragePain() >= PAINFUL_THRESHOLD) {
      return PooSuggestion.PAINFUL_DEFECATION;
    }

    // 변비 판정 (딱딱하고 배변 횟수가 적음)
    if (shape == ToiletShape.ROCK && pooReport.getNumberOfRecords() < CONSTIPATION_THRESHOLD) {
      return PooSuggestion.CONSTIPATION;
    }

    // 설사 판정 (묽고 잦음)
    if (shape == ToiletShape.PORRIDGE && pooReport.getNumberOfRecords() > CONSTIPATION_THRESHOLD) {
      return PooSuggestion.DIARRHEA;
    }

    // 딱딱한 변
    if (shape == ToiletShape.ROCK) {
      return PooSuggestion.HARD_STOOL;
    }

    // 묽은 변
    if (shape == ToiletShape.PORRIDGE || shape == ToiletShape.CREAM) {
      return PooSuggestion.SOFT_STOOL;
    }

    // 배변 시간이 긴 경우
    if (pooReport.getAverageDuration() > NORMAL_DURATION_THRESHOLD) {
      return PooSuggestion.LONG_DEFECATION_TIME;
    }

    // 정상적인 경우들
    if ((pooReport.getLevel() == PooEvaluationLevel.GOOD
            || pooReport.getLevel() == PooEvaluationLevel.VERY_GOOD)
        && pooReport.getAverageDuration() <= NORMAL_DURATION_THRESHOLD
        && pooReport.getAveragePain() <= PAINFUL_THRESHOLD) {
      if (pooReport.hasGoodShape()) {
        return PooSuggestion.IDEAL_SHAPE;
      }

      if (pooReport.hasGoodColor()) {
        return PooSuggestion.HEALTHY_COLOR;
      }

      return PooSuggestion.HEALTHY_REGULAR;
    }

    // 통증 없는 배변
    if (pooReport.getAveragePain() <= PAINFUL_THRESHOLD) {
      return PooSuggestion.PAIN_FREE;
    }

    // 적절한 시간 내 배변
    if (pooReport.getAverageDuration() <= NORMAL_DURATION_THRESHOLD) {
      return PooSuggestion.NORMAL_DURATION;
    }

    // 기본값 (정상적인 규칙적 배변)
    return PooSuggestion.HEALTHY_REGULAR;
  }

  private List<HabitSuggestion> generateHabitSuggestions(
      ActivityReport activityReport, PooReport pooReport) {

    List<HabitSuggestion> result = new ArrayList<>();

    // 스트레스 레벨 평가
    if (activityReport.hasStress()) {
      result.add(HabitSuggestion.HIGH_STRESS_LEVEL);
    } else if (activityReport.isStressWellManaged()) {
      result.add(HabitSuggestion.STRESS_MANAGEMENT);
    }

    // 알코올 섭취 평가
    if (pooReport.drunkAlcohol()) {
      result.add(HabitSuggestion.EXCESSIVE_ALCOHOL);
    }

    // 화장실 사용 습관 평가
    if (pooReport.getAverageDuration() > NORMAL_DURATION_THRESHOLD) {
      result.add(HabitSuggestion.LONG_TOILET_TIME);
    } else if (pooReport.getAverageDuration() <= SHORT_DURATION_THRESHOLD) {
      result.add(HabitSuggestion.SHORT_TOILET_TIME);
    }

    // 8. 긍정적 습관 강화 (이미 좋은 습관이 있는 경우)
    if (result.isEmpty() || hasOnlyPositiveHabits(result)) {
      addPositiveReinforcementSuggestions(result, activityReport, pooReport);
    }

    return result;
  }
}
