package depromeet.lessonfour.server.report.domain.policy;

import static depromeet.lessonfour.server.report.domain.vo.Suggestion.StoolSuggestion.BLOODY_STOOL;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.report.domain.vo.ActivityReport;
import depromeet.lessonfour.server.report.domain.vo.DayType;
import depromeet.lessonfour.server.report.domain.vo.Suggestion;
import depromeet.lessonfour.server.report.domain.vo.Suggestion.HabitSuggestion;
import depromeet.lessonfour.server.report.domain.vo.Suggestion.StoolSuggestion;
import depromeet.lessonfour.server.report.domain.vo.Suggestion.WaterSuggestion;
import depromeet.lessonfour.server.report.domain.vo.ToiletEvaluationLevel;
import depromeet.lessonfour.server.report.domain.vo.ToiletReport;
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

  public Suggestion evaluate(ActivityReport activityReport, ToiletReport toiletReport) {
    WaterSuggestion waterSuggestion = generateWaterSuggestions(activityReport);
    StoolSuggestion stoolSuggestion = generatePooSuggestions(toiletReport);
    List<HabitSuggestion> habitSuggestions = generateHabitSuggestions(activityReport, toiletReport);

    return new Suggestion(waterSuggestion, stoolSuggestion, habitSuggestions);
  }

  private WaterSuggestion generateWaterSuggestions(ActivityReport activityReport) {
    WaterEvaluation water =
        activityReport.getWaterEvaluations().stream()
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
      List<HabitSuggestion> result, ActivityReport activityReport, ToiletReport toiletReport) {

    if (activityReport.isStressWellManaged()) {
      result.add(HabitSuggestion.STRESS_MANAGEMENT);
    }

    if (toiletReport.getStoolScore() >= VERY_GOOD_CONDITION_THRESHOLD) {
      result.add(HabitSuggestion.REGULAR_TOILET_HABITS);
    }
  }

  private StoolSuggestion generatePooSuggestions(ToiletReport toiletReport) {

    ToiletShape shape = toiletReport.getMostFrequentShape();
    // 가장 심각한 증상부터 체크
    if (toiletReport.hasBlood()) {
      return BLOODY_STOOL;
    }

    if (toiletReport.hasAbnormalColor()) {
      return StoolSuggestion.COLOR_ABNORMAL;
    }

    // 배변 시 통증이 있는 경우
    if (toiletReport.getAveragePain() >= PAINFUL_THRESHOLD) {
      return StoolSuggestion.PAINFUL_DEFECATION;
    }

    // 변비 판정 (딱딱하고 배변 횟수가 적음)
    if (shape == ToiletShape.ROCK && toiletReport.getNumberOfRecords() < CONSTIPATION_THRESHOLD) {
      return StoolSuggestion.CONSTIPATION;
    }

    // 설사 판정 (묽고 잦음)
    if (shape == ToiletShape.PORRIDGE
        && toiletReport.getNumberOfRecords() > CONSTIPATION_THRESHOLD) {
      return StoolSuggestion.DIARRHEA;
    }

    // 딱딱한 변
    if (shape == ToiletShape.ROCK) {
      return StoolSuggestion.HARD_STOOL;
    }

    // 묽은 변
    if (shape == ToiletShape.PORRIDGE || shape == ToiletShape.CREAM) {
      return StoolSuggestion.SOFT_STOOL;
    }

    // 배변 시간이 긴 경우
    if (toiletReport.getAverageDuration() > NORMAL_DURATION_THRESHOLD) {
      return StoolSuggestion.LONG_DEFECATION_TIME;
    }

    // 정상적인 경우들
    if ((toiletReport.getLevel() == ToiletEvaluationLevel.GOOD
            || toiletReport.getLevel() == ToiletEvaluationLevel.VERY_GOOD)
        && toiletReport.getAverageDuration() <= NORMAL_DURATION_THRESHOLD
        && toiletReport.getAveragePain() <= PAINFUL_THRESHOLD) {
      if (toiletReport.hasGoodShape()) {
        return StoolSuggestion.IDEAL_SHAPE;
      }

      if (toiletReport.hasGoodColor()) {
        return StoolSuggestion.HEALTHY_COLOR;
      }

      return StoolSuggestion.HEALTHY_REGULAR;
    }

    // 통증 없는 배변
    if (toiletReport.getAveragePain() <= PAIN_MILD_THRESHOLD) {
      return StoolSuggestion.PAIN_FREE;
    }

    // 적절한 시간 내 배변
    if (toiletReport.getAverageDuration() <= NORMAL_DURATION_THRESHOLD) {
      return StoolSuggestion.NORMAL_DURATION;
    }

    // 기본값 (정상적인 규칙적 배변)
    return StoolSuggestion.HEALTHY_REGULAR;
  }

  private List<HabitSuggestion> generateHabitSuggestions(
      ActivityReport activityReport, ToiletReport toiletReport) {

    List<HabitSuggestion> result = new ArrayList<>();

    // 스트레스 레벨 평가
    if (activityReport.hasStress()) {
      result.add(HabitSuggestion.HIGH_STRESS_LEVEL);
    } else if (activityReport.isStressWellManaged()) {
      result.add(HabitSuggestion.STRESS_MANAGEMENT);
    }

    // 알코올 섭취 평가
    if (toiletReport.drunkAlcohol()) {
      result.add(HabitSuggestion.EXCESSIVE_ALCOHOL);
    }

    // 화장실 사용 습관 평가
    if (toiletReport.getAverageDuration() > NORMAL_DURATION_THRESHOLD) {
      result.add(HabitSuggestion.LONG_TOILET_TIME);
    } else if (toiletReport.getAverageDuration() <= SHORT_DURATION_THRESHOLD) {
      result.add(HabitSuggestion.SHORT_TOILET_TIME);
    }

    // 8. 긍정적 습관 강화 (이미 좋은 습관이 있는 경우)
    if (result.isEmpty() || hasOnlyPositiveHabits(result)) {
      addPositiveReinforcementSuggestions(result, activityReport, toiletReport);
    }

    return result;
  }
}
