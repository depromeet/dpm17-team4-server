package depromeet.lessonfour.server.report.domain.policy;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.activityrecord.domain.entity.FoodRecord;
import depromeet.lessonfour.server.activityrecord.domain.vo.MealTime;
import depromeet.lessonfour.server.report.domain.vo.DayType;
import depromeet.lessonfour.server.report.domain.vo.FoodEvaluation;

@Component
public class FoodEvaluationPolicy {

  private static final int DEFAULT_DANGEROUS_THRESHOLD = 80;

  public FoodEvaluation calculate(List<FoodRecord> records, DayType dayType) {
    if (records == null || records.isEmpty()) {
      return new FoodEvaluation(false, Map.of(), dayType, Set.of());
    }

    // 1) 위험 음식 존재 여부
    boolean dangerous =
        records.stream()
            .filter(r -> r != null && r.getFood() != null)
            .anyMatch(r -> r.getFood().isDangerous(DEFAULT_DANGEROUS_THRESHOLD));

    // 2) 식사시간별 음식 이름 리스트
    Map<MealTime, List<String>> foodsByMealTime =
        records.stream()
            .filter(
                r ->
                    r != null
                        && r.getMealTime() != null
                        && r.getFood() != null
                        && r.getFood().getName() != null)
            .collect(
                Collectors.groupingBy(
                    FoodRecord::getMealTime,
                    () -> new java.util.EnumMap<>(MealTime.class),
                    Collectors.mapping(r -> r.getFood().getName(), Collectors.toList())));

    // 3) 위험 음식이 있었던 식사시간 집합
    Set<MealTime> dangerousMealTimes =
        records.stream()
            .filter(r -> r != null && r.getMealTime() != null && r.getFood() != null)
            .filter(r -> r.getFood().isDangerous(DEFAULT_DANGEROUS_THRESHOLD))
            .map(FoodRecord::getMealTime)
            .collect(Collectors.toSet());

    return new FoodEvaluation(dangerous, foodsByMealTime, dayType, dangerousMealTimes);
  }
}
