package depromeet.lessonfour.server.report.domain.policy;

import static depromeet.lessonfour.server.report.domain.vo.Suggestion.ToiletSuggestion.BLOODY_STOOL;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.report.domain.vo.DailyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.DailyToiletReport;
import depromeet.lessonfour.server.report.domain.vo.DayType;
import depromeet.lessonfour.server.report.domain.vo.StressEvaluation;
import depromeet.lessonfour.server.report.domain.vo.Suggestion;
import depromeet.lessonfour.server.report.domain.vo.Suggestion.HabitSuggestion;
import depromeet.lessonfour.server.report.domain.vo.Suggestion.ToiletSuggestion;
import depromeet.lessonfour.server.report.domain.vo.Suggestion.WaterSuggestion;
import depromeet.lessonfour.server.report.domain.vo.ToiletEvaluationLevel;
import depromeet.lessonfour.server.report.domain.vo.WaterEvaluation;
import depromeet.lessonfour.server.report.domain.vo.monthly.MonthlyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.monthly.MonthlyToiletReport;
import depromeet.lessonfour.server.report.domain.vo.weekly.WeeklyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.weekly.WeeklyToiletReport;
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

  // 주간용
  public Suggestion evaluateWeekly(
      WeeklyActivityReport weeklyActivityReport, WeeklyToiletReport weeklyToiletReport) {

    WaterSuggestion waterSuggestion = generateWeeklyWaterSuggestion(weeklyActivityReport);
    ToiletSuggestion toiletSuggestion = generateWeeklyPooSuggestions(weeklyToiletReport);
    List<HabitSuggestion> habitSuggestions =
        generateWeeklyHabitSuggestions(weeklyActivityReport, weeklyToiletReport);

    return new Suggestion(waterSuggestion, toiletSuggestion, habitSuggestions);
  }

  private WaterSuggestion generateWeeklyWaterSuggestion(WeeklyActivityReport weeklyActivityReport) {
    // 일주일 동안 waterEvaluations를 다 flatten 한 뒤
    List<WaterEvaluation> all =
        weeklyActivityReport.getDailyReports().stream()
            .flatMap(r -> r.getWaterEvaluations().stream())
            .toList();

    if (all.isEmpty()) {
      return WaterSuggestion.NONE;
    }

    // 평균 컵 수 (quantity)로 레벨 판단
    double avgQuantity = all.stream().mapToInt(WaterEvaluation::getQuantity).average().orElse(0);

    // 나중에 공통 util로 뺄 수 있음.
    if (avgQuantity >= 8) {
      return WaterSuggestion.HIGH;
    } else if (avgQuantity >= 5) {
      return WaterSuggestion.MEDIUM;
    } else if (avgQuantity > 0) {
      return WaterSuggestion.LOW;
    } else {
      return WaterSuggestion.NONE;
    }
  }

  private ToiletSuggestion generateWeeklyPooSuggestions(WeeklyToiletReport weekly) {
    // 기존 generatePooSuggestions(DailyToiletReport) 로직을
    // WeeklyToiletReport의 평균/any 기반 API로 그대로 적용

    // 무기록: 분석 스킵
    if (weekly.getNumberOfRecords() == 0) {
      return null; // 상위에서 null-safe 처리
    }

    // 가장 심각한 증상부터 체크
    if (weekly.hasBlood()) {
      return ToiletSuggestion.BLOODY_STOOL;
    }

    if (weekly.hasAbnormalColor()) {
      return ToiletSuggestion.COLOR_ABNORMAL;
    }

    // 배변 시 통증 (주간 평균)
    if (weekly.getAveragePain() >= PAINFUL_THRESHOLD) {
      return ToiletSuggestion.PAINFUL_DEFECATION;
    }

    // 변비 판정: 기록이 적고/딱딱한 모양이 많은지는 WeeklyToiletReport에 별도 메서드 추가해서 더 고도화 가능
    if (weekly.getNumberOfRecords() < CONSTIPATION_THRESHOLD) {
      return ToiletSuggestion.CONSTIPATION;
    }

    // 주간 전체적으로 묽은 변이 많다/딱딱한 변이 많다 판단도
    // 필요하면 WeeklyToiletReport에 카운트 메서드 추가해서 적용 가능

    // 배변 시간이 긴 경우 (주간 평균 시간)
    if (weekly.getAverageDuration() > NORMAL_DURATION_THRESHOLD) {
      return ToiletSuggestion.LONG_DEFECATION_TIME;
    }

    // 정상적인 경우들 (주간 종합 레벨 + 평균)
    ToiletEvaluationLevel level = weekly.getLevel();
    if ((level == ToiletEvaluationLevel.GOOD || level == ToiletEvaluationLevel.VERY_GOOD)
        && weekly.getAverageDuration() <= NORMAL_DURATION_THRESHOLD
        && weekly.getAveragePain() <= PAINFUL_THRESHOLD) {

      if (weekly.hasGoodShape()) {
        return ToiletSuggestion.IDEAL_SHAPE;
      }
      if (weekly.hasGoodColor()) {
        return ToiletSuggestion.HEALTHY_COLOR;
      }
      return ToiletSuggestion.HEALTHY_REGULAR;
    }

    // 통증 거의 없는 주간
    if (weekly.getAveragePain() <= PAIN_MILD_THRESHOLD) {
      return ToiletSuggestion.PAIN_FREE;
    }

    // 적절한 시간 내 배변
    if (weekly.getAverageDuration() <= NORMAL_DURATION_THRESHOLD) {
      return ToiletSuggestion.NORMAL_DURATION;
    }

    return ToiletSuggestion.HEALTHY_REGULAR;
  }

  private List<HabitSuggestion> generateWeeklyHabitSuggestions(
      WeeklyActivityReport weeklyActivityReport, WeeklyToiletReport weeklyToiletReport) {

    List<HabitSuggestion> result = new ArrayList<>();

    // 스트레스 많은 날이 일정 횟수 이상이면 HIGH_STRESS_LEVEL
    if (weeklyActivityReport.highStressDays() >= 3) {
      result.add(HabitSuggestion.HIGH_STRESS_LEVEL);
    } else if (weeklyActivityReport.highStressDays() == 0) {
      result.add(HabitSuggestion.STRESS_MANAGEMENT);
    }

    // 술을 마신 날이 있으면
    if (weeklyToiletReport.drunkAlcohol()) {
      result.add(HabitSuggestion.EXCESSIVE_ALCOHOL);
    }

    // 평균 배변 시간이 너무 길거나 너무 짧으면
    if (weeklyToiletReport.getAverageDuration() > NORMAL_DURATION_THRESHOLD) {
      result.add(HabitSuggestion.LONG_TOILET_TIME);
    } else if (weeklyToiletReport.getAverageDuration() <= SHORT_DURATION_THRESHOLD) {
      result.add(HabitSuggestion.SHORT_TOILET_TIME);
    }

    // 긍정적 습관 강화
    if (result.isEmpty() || hasOnlyPositiveHabits(result)) {
      // 스트레스 잘 관리
      if (weeklyActivityReport.highStressDays() == 0) {
        result.add(HabitSuggestion.STRESS_MANAGEMENT);
      }
      // 배변 점수 매우 좋음
      if (weeklyToiletReport.getAverageScore() >= VERY_GOOD_CONDITION_THRESHOLD) {
        result.add(HabitSuggestion.REGULAR_TOILET_HABITS);
      }
    }

    return result;
  }

  // 월간용
  public Suggestion evaluateMonthly(
      MonthlyActivityReport monthlyActivityReport, MonthlyToiletReport monthlyToiletReport) {

    // 1) 물: 월 평균 컵 수 → 레벨(주간과 동일 기준)
    var allWater =
        monthlyActivityReport.weeklyGroups().stream()
            .flatMap(g -> g.dailyReports().stream())
            .flatMap(d -> d.getWaterEvaluations().stream())
            .toList();

    Suggestion.WaterSuggestion waterSuggestion;
    if (allWater.isEmpty()) {
      waterSuggestion = Suggestion.WaterSuggestion.NONE;
    } else {
      double avgQ = allWater.stream().mapToInt(w -> w.getQuantity()).average().orElse(0);
      // 주간과 같은 컵수 기준(8/5)을 재사용 — 공통 util로 뺄 수 있음
      if (avgQ >= 8) waterSuggestion = Suggestion.WaterSuggestion.HIGH;
      else if (avgQ >= 5) waterSuggestion = Suggestion.WaterSuggestion.MEDIUM;
      else if (avgQ > 0) waterSuggestion = Suggestion.WaterSuggestion.LOW;
      else waterSuggestion = Suggestion.WaterSuggestion.NONE;
    }

    // 2) 배변: 월간 분포 → 추정치로 동일 의사결정 순서 적용
    double avgPain = MonthlyToiletStats.avgPain(monthlyToiletReport.painDistribution());
    double avgMinutes = MonthlyToiletStats.avgMinutes(monthlyToiletReport.timeDistribution());
    int totalRecords = MonthlyToiletStats.totalRecords(monthlyToiletReport.timeDistribution());
    boolean hasBlood = MonthlyToiletStats.hasBlood(monthlyToiletReport.colorCount());
    boolean colorAbn = MonthlyToiletStats.hasAbnormalColor(monthlyToiletReport.colorCount());
    boolean goodShape = MonthlyToiletStats.goodShapeMostly(monthlyToiletReport.shapeCount());
    boolean goodColor = MonthlyToiletStats.goodColorMostly(monthlyToiletReport.colorCount());

    Suggestion.ToiletSuggestion toiletSuggestion = null;
    if (totalRecords > 0) {
      if (hasBlood) {
        toiletSuggestion = Suggestion.ToiletSuggestion.BLOODY_STOOL;
      } else if (colorAbn) {
        toiletSuggestion = Suggestion.ToiletSuggestion.COLOR_ABNORMAL;
      } else if (avgPain >= PAINFUL_THRESHOLD) {
        toiletSuggestion = Suggestion.ToiletSuggestion.PAINFUL_DEFECATION;
      } else if (totalRecords < CONSTIPATION_THRESHOLD) {
        toiletSuggestion = Suggestion.ToiletSuggestion.CONSTIPATION;
      } else if (avgMinutes > NORMAL_DURATION_THRESHOLD) {
        toiletSuggestion = Suggestion.ToiletSuggestion.LONG_DEFECATION_TIME;
      } else {
        if (goodShape)
          toiletSuggestion = Suggestion.ToiletSuggestion.IDEAL_SHAPE;
        else if (goodColor)
          toiletSuggestion = Suggestion.ToiletSuggestion.HEALTHY_COLOR;
        else
          toiletSuggestion = Suggestion.ToiletSuggestion.HEALTHY_REGULAR;
      }
    }

    // 3) 습관 권고: 스트레스/물 기준(주간과 유사)
    List<Suggestion.HabitSuggestion> habits = new ArrayList<>();
    long highStressDays =
        monthlyActivityReport.weeklyGroups().stream()
            .flatMap(g -> g.dailyReports().stream())
            .filter(
                d ->
                    d.getStressEvaluation() != null
                        && d.getStressEvaluation().ordinal() >= StressEvaluation.MEDIUM.ordinal())
            .count();

    if (highStressDays >= 3) {
      habits.add(Suggestion.HabitSuggestion.HIGH_STRESS_LEVEL);
    } else {
      habits.add(Suggestion.HabitSuggestion.STRESS_MANAGEMENT);
    }

    if (waterSuggestion == Suggestion.WaterSuggestion.LOW
        || waterSuggestion == Suggestion.WaterSuggestion.NONE) {
      habits.add(Suggestion.HabitSuggestion.LONG_TOILET_TIME); // 물 부족 시 행동 권고 대체
    } else {
      habits.add(Suggestion.HabitSuggestion.REGULAR_TOILET_HABITS);
    }

    return new Suggestion(waterSuggestion, toiletSuggestion, habits);
  }
}
