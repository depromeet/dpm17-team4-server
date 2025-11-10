package depromeet.lessonfour.server.report.domain.policy;

import static depromeet.lessonfour.server.activityrecord.domain.vo.MealTime.BREAKFAST;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import depromeet.lessonfour.server.report.domain.vo.StressEvaluation;
import depromeet.lessonfour.server.report.domain.vo.SuggestionRule;
import depromeet.lessonfour.server.report.domain.vo.SuggestionType;
import depromeet.lessonfour.server.report.domain.vo.ToiletEvaluation;
import depromeet.lessonfour.server.report.domain.vo.WaterEvaluation;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletColor;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletShape;

@Configuration
public class SuggestionRules {

  @Configuration
  static class WaterRules {
    /** 수분 섭취량이 [부족]인 경우 */
    @Bean
    public SuggestionRule checkWaterLack() {
      return (context, types) -> {
        boolean isWaterLack =
            context.getWaterEvaluations().stream().anyMatch(WaterEvaluation::isLack);

        if (isWaterLack) {
          types.add(SuggestionType.WATER_LACK);
        }
      };
    }

    /** 수분 섭취가 [보통] or [부족]인 경우 */
    @Bean
    public SuggestionRule checkWaterNormalOrLack() {
      return (context, types) -> {
        boolean hasNormalOrLack =
            context.getWaterEvaluations().stream()
                .anyMatch(evaluation -> evaluation.isNormal() || evaluation.isLack());

        if (hasNormalOrLack) {
          types.add(SuggestionType.WATER_LACK);
        }
      };
    }
  }

  @Configuration
  static class FoodRules {
    /** 채소나 과일 섭취가 2일 이상 연속 없는 경우 */
    @Bean
    public SuggestionRule checkFiberLack() {
      return (context, types) -> {
        // TODO : 채소/과일 정의 필요
      };
    }

    /** 식단에 [아침] and [자극적인 음식]이 있는 경우 */
    @Bean
    public SuggestionRule checkSpicyFoodAtBreakfast() {
      return (context, types) -> {
        boolean hasSpicyFoodAtBreakfast =
            context.getFoodEvaluations().stream().anyMatch(fe -> fe.isMealDangerous(BREAKFAST));

        if (hasSpicyFoodAtBreakfast) {
          types.add(SuggestionType.SPICY_FOOD_AT_BREAKFAST);
        }
      };
    }

    /** 식단에 유제품이 있는 경우 */
    @Bean
    public SuggestionRule checkDailyProduct() {
      List<String> dailyProducts = List.of("우유", "치즈", "요거트", "버터");

      return (context, types) -> {
        boolean hasDailyProduct =
            context.getFoodEvaluations().stream()
                .flatMap(evaluation -> evaluation.getFoodsByMealTime().values().stream())
                .flatMap(List::stream)
                .anyMatch(food -> dailyProducts.stream().anyMatch(food::contains));

        if (hasDailyProduct) {
          types.add(SuggestionType.DAILY_PRODUCT);
        }
      };
    }

    /** 식단에 카페인이 있는 경우 */
    @Bean
    public SuggestionRule checkCaffeine() {
      List<String> caffeineFoods = List.of("커피", "에너지드링크", "홍차", "녹차", "콜라");

      return (context, types) -> {
        boolean hasCaffeine =
            context.getFoodEvaluations().stream()
                .flatMap(evaluation -> evaluation.getFoodsByMealTime().values().stream())
                .flatMap(List::stream)
                .anyMatch(food -> caffeineFoods.stream().anyMatch(food::contains));

        if (hasCaffeine) {
          types.add(SuggestionType.CAFFEINE);
        }
      };
    }

    /** 식단에 알콜이 있는 경우 */
    @Bean
    public SuggestionRule checkAlcohol() {
      List<String> alcoholFoods = List.of("맥주", "소주", "와인", "위스키", "막걸리");

      return (context, types) -> {
        boolean hasAlcohol =
            context.getFoodEvaluations().stream()
                .flatMap(evaluation -> evaluation.getFoodsByMealTime().values().stream())
                .flatMap(List::stream)
                .anyMatch(food -> alcoholFoods.stream().anyMatch(food::contains));

        if (hasAlcohol) {
          types.add(SuggestionType.ALCOHOL);
        }
      };
    }

    /** 하루 식단에 단백질 음식 비중이 높은 경우 */
    @Bean
    public SuggestionRule checkHighProtein() {
      return (context, types) -> {
        // TODO : 단백질 음식 정의 및 비중 계산 필요
      };
    }

    /** 식단에 기름진 음식 비중이 높은 경우 */
    @Bean
    public SuggestionRule checkHighFat() {
      return (context, types) -> {
        // TODO : 기름진 음식 정의 및 비중 계산 필요
      };
    }

    /** 식단에 맵고 자극적인 음식 비중이 높은 경우 */
    @Bean
    public SuggestionRule checkHighSpicy() {
      return (context, types) -> {
        // TODO : 맵고 자극적인 음식 정의 및 비중 계산 필요
      };
    }

    /**
     * 식단에 과도하게 많은 음식이 기록된 경우
     *
     * <ul>
     *   <li>한 끼 음식 개수 5개 이상
     *   <li>그 중 기름진 음식/탄수화물/고기류 포함 비중이 높을 때
     *   <li>하루 2회 이상 동시간대 음식 5개 이상 기록
     */
    @Bean
    public SuggestionRule checkOvereat() {
      return (context, types) -> {
        // TODO : 과식 정의 및 계산 필요
      };
    }

    /**
     * 유산균 섭취 분석
     *
     * <p>1) 배변 상태 기반
     *
     * <ul>
     *   <li>변비 증상이 있는 경우 = [못 쌌어요], [토끼똥], [소요시간 >= 10]
     *   <li>배변이 불규칙한 경우 = [3일 이상 기록 없음]
     *   <li>설사를 기록한 경우 = 배변 모양이 [죽], [물]
     *   <li>복통 정도가 [70%] 이상인 경우
     * </ul>
     *
     * 2) 생활 기반
     *
     * <ul>
     *   <li>기름진 음식, 튀김류, 매운 음식 이틀 이상 기록
     *   <li>채소나 과일 섭취가 2일 이상 연속 없는 경우
     *   <li>특이 사항에 [약]이나 [항생제]가 있는 경우
     * </ul>
     */
    @Bean
    public SuggestionRule checkLactobacillus() {
      final int TOILET_TIME_THRESHOLD = 10;
      return (context, types) -> {
        List<ToiletEvaluation> toiletEvaluations = context.getToiletEvaluations();

        // 1. 변비 검사
        boolean isConstipation =
            toiletEvaluations.stream()
                .anyMatch(
                    evaluation ->
                        evaluation.failed()
                            || evaluation.getShape() == ToiletShape.RABBIT
                            || evaluation.getDuration() >= TOILET_TIME_THRESHOLD);

        // 2. 기록 횟수 검사
        final int RECORD_LACK_DAYS_THRESHOLD = 3;
        boolean isIrregular = toiletEvaluations.size() < RECORD_LACK_DAYS_THRESHOLD;

        // 3. 설사 검사
        boolean isDiarrhea =
            toiletEvaluations.stream()
                .anyMatch(
                    evaluation ->
                        evaluation.getShape() == ToiletShape.PORRIDGE
                            || evaluation.getShape() == ToiletShape.WATER);

        // 4. 복통 검사
        final double PAIN_THRESHOLD = 70.0;
        boolean hasSeverePain =
            toiletEvaluations.stream()
                .anyMatch(evaluation -> evaluation.getPain() >= PAIN_THRESHOLD);

        // 5. [약] or [항생제] 검사
        boolean hasMedicineNote =
            toiletEvaluations.stream()
                .anyMatch(
                    evaluation -> {
                      String note = evaluation.getNote();
                      return note != null && (note.contains("약") || note.contains("항생제"));
                    });

        if (isConstipation || isIrregular || isDiarrhea || hasSeverePain || hasMedicineNote) {
          types.add(SuggestionType.LACTOBACILLUS);
        }
        // TODO : 기름진 음식, 튀김류, 매운 음식, 채소, 과일 정의 필요
      };
    }

    /**
     * 해조류 권장
     *
     * <p>식이섬유 섭취가 부족하거나 없는 경우
     */
    @Bean
    public SuggestionRule checkSeaweed() {
      return (context, types) -> {
        // TODO : 식이섬유 정의 필요
      };
    }

    /**
     * 잡곡 권장
     *
     * <p>식단에 [흰쌀밥]이 있는 경우
     */
    @Bean
    public SuggestionRule checkMixedGrain() {
      List<String> foods = List.of("흰쌀밥", "백미밥", "흰 쌀밥", "백미");

      return (context, types) -> {
        boolean hasWhiteRice =
            context.getFoodEvaluations().stream()
                .flatMap(evaluation -> evaluation.getFoodsByMealTime().values().stream())
                .flatMap(List::stream)
                .anyMatch(food -> foods.stream().anyMatch(food::contains));

        if (hasWhiteRice) {
          types.add(SuggestionType.MIXED_GRAIN);
        }
      };
    }

    /** 아침 식단이 누락된 경우 */
    @Bean
    public SuggestionRule checkBreakfastLack() {
      return (context, types) -> {
        boolean isBreakfastMissing =
            context.getFoodEvaluations().stream()
                .noneMatch(evaluation -> evaluation.getFoodsByMealTime().containsKey(BREAKFAST));

        if (isBreakfastMissing) {
          types.add(SuggestionType.BREAKFAST_LACK);
        }
      };
    }
  }

  @Configuration
  static class StressRules {
    /** 스트레스가 [조금 심함] or [심함]인 경우 */
    @Bean
    public SuggestionRule checkStress() {
      return (context, types) -> {
        boolean hasModerateStress =
            context.getStressEvaluations().stream().anyMatch(StressEvaluation::isHigh);

        if (hasModerateStress) {
          types.add(SuggestionType.HIGH_STRESS);
        }
      };
    }

    /**
     * 스트레칭 권장
     *
     * <p>스트레스가 [높음] or [매우 높음]인 경우
     */
    @Bean
    public SuggestionRule checkStretching() {
      return (context, types) -> {
        boolean hasHighStress =
            context.getStressEvaluations().stream().anyMatch(StressEvaluation::isHigh);

        if (hasHighStress) {
          types.add(SuggestionType.STRETCHING);
        }
      };
    }
  }

  @Configuration
  static class ToiletRules {
    /** 배변 기록을 주 3일 이하로 기록한 경우 */
    @Bean
    public SuggestionRule checkRecordLack() {
      final int RECORD_LACK_THRESHOLD = 3;

      return (context, types) -> {
        if (context.getToiletEvaluations().size() <= RECORD_LACK_THRESHOLD) {
          types.add(SuggestionType.RECORD_LACK);
        }
      };
    }

    /**
     * 장염이나 설사 증상이 있는 경우
     *
     * <ul>
     *   <li>배변 모양이 [죽], [물]
     *   <li>특이사항에 [장염], [설사], [복통]이 있는 경우
     */
    @Bean
    public SuggestionRule checkEnteritis() {
      return (context, types) -> {
        List<ToiletEvaluation> toiletEvaluations = context.getToiletEvaluations();

        boolean hasShapeIssue =
            toiletEvaluations.stream()
                .anyMatch(
                    evaluation ->
                        evaluation.getShape() == ToiletShape.PORRIDGE
                            || evaluation.getShape() == ToiletShape.WATER);

        boolean hasNoteIssue =
            toiletEvaluations.stream()
                .anyMatch(
                    evaluation -> {
                      String note = evaluation.getNote();
                      return note != null
                          && (note.contains("장염") || note.contains("설사") || note.contains("복통"));
                    });

        if (hasShapeIssue || hasNoteIssue) {
          types.add(SuggestionType.ENTERITIS);
        }
      };
    }

    /**
     * 부드러운 음식 추천
     *
     * <ul>
     *   <li>배변 점수가 50점 이하인 경우
     *   <li>배변 모양이 [죽], [물]
     *   <li>복통 정도가 [70%] 이상인 경우
     *   <li>특이사항에 [설사약], [복통]이 기록된 경우
     */
    @Bean
    public SuggestionRule checkSoftFood() {
      final double SCORE_THRESHOLD = 50.0;
      final double PAIN_THRESHOLD = 70.0;

      return (context, types) -> {
        List<ToiletEvaluation> toiletEvaluations = context.getToiletEvaluations();

        boolean lowScore =
            toiletEvaluations.stream()
                .anyMatch(evaluation -> evaluation.getScore() <= SCORE_THRESHOLD);

        boolean hasShapeIssue =
            toiletEvaluations.stream()
                .anyMatch(
                    evaluation ->
                        evaluation.getShape() == ToiletShape.PORRIDGE
                            || evaluation.getShape() == ToiletShape.WATER);

        boolean hasPain =
            toiletEvaluations.stream()
                .anyMatch(evaluation -> evaluation.getPain() >= PAIN_THRESHOLD);

        boolean hasNoteIssue =
            toiletEvaluations.stream()
                .anyMatch(
                    evaluation -> {
                      String note = evaluation.getNote();
                      return note != null && (note.contains("설사약") || note.contains("복통"));
                    });

        if (lowScore || hasShapeIssue || hasPain || hasNoteIssue) {
          types.add(SuggestionType.SOFT_FOOD);
        }
      };
    }

    /**
     * 배변 시간이 불규칙한 경우
     *
     * <ul>
     *   <li>2일 이상 연속으로 [못 쌌어요] 기록한 경우
     *   <li>배변 모양이 [토끼] or [옥수수]를 7일중 3회 이상 기록한 경우
     *   <li>복통 정도가 [70%] 이상을 최근 7일중 3회 이상 기록한 경우
     *   <li>소요 시간 [10분 이상]을 최근 7일중 2회 이상 기록한 경우
     *   <li>특이사항에 [변비약], [복통]이 기록된 경우
     */
    @Bean
    public SuggestionRule checkInconsistentTime() {
      return null;
    }

    /** 생리 : 특이사항에 [생리]가 기록된 경우 */
    @Bean
    public SuggestionRule checkMenstruation() {
      return (context, types) -> {
        boolean hasMenstruation =
            context.getToiletEvaluations().stream()
                .anyMatch(
                    evaluation -> {
                      String note = evaluation.getNote();
                      return note != null && note.contains("생리");
                    });

        if (hasMenstruation) {
          types.add(SuggestionType.MENSTRUATION);
        }
      };
    }

    /** 배변 기록 주기가 긴 경우 */
    @Bean
    public SuggestionRule checkLongToiletRecordInterval() {
      return (context, types) -> {
        // TODO : 배변 기록 주기 정의 필요
      };
    }

    /** 전문가 상담이 필요한 경우 */
    @Bean
    public SuggestionRule checkConsultationNeeded() {
      final List<ToiletColor> abnormalShapes =
          List.of(ToiletColor.RED, ToiletColor.BLACK, ToiletColor.GRAY);

      final int BAD_SCORE_THRESHOLD = 25;

      return (context, types) -> {
        boolean hasAbnormalShape =
            context.getToiletEvaluations().stream()
                .anyMatch(evaluation -> abnormalShapes.contains(evaluation.getColor()));

        boolean hasBadScore =
            context.getToiletEvaluations().stream()
                .anyMatch(evaluation -> evaluation.getScore() <= BAD_SCORE_THRESHOLD);

        if (hasAbnormalShape || hasBadScore) {
          types.add(SuggestionType.NEED_MEDICAL_EXAMINATION);
        }
      };
    }

    /** 화장실 소요 시간이 [10분] 이상인 경우 */
    @Bean
    public SuggestionRule checkLongToiletTime() {
      final int TOILET_TIME_THRESHOLD = 10;

      return (context, types) -> {
        boolean hasLongToiletTime =
            context.getToiletEvaluations().stream()
                .anyMatch(evaluation -> evaluation.getDuration() >= TOILET_TIME_THRESHOLD);

        if (hasLongToiletTime) {
          types.add(SuggestionType.LONG_TOILET_TIME);
        }
      };
    }
  }
}
