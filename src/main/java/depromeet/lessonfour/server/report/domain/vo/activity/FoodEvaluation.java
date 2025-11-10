package depromeet.lessonfour.server.report.domain.vo.activity;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import depromeet.lessonfour.server.activityrecord.domain.entity.FoodRecord;
import depromeet.lessonfour.server.activityrecord.domain.vo.MealTime;
import lombok.Getter;

@Getter
public class FoodEvaluation {

  private static final int DEFAULT_DANGEROUS_THRESHOLD = 80;

  private final DayType dayType;
  private final boolean dangerous;
  private final Map<MealTime, List<String>> foodsByMealTime;
  private final Set<MealTime> dangerousMealTimes;

  public FoodEvaluation(
      boolean dangerous,
      Map<MealTime, List<String>> foodsByMealTime,
      DayType dayType,
      Set<MealTime> dangerousMealTimes) {
    this.dangerous = dangerous;
    this.dayType = Objects.requireNonNull(dayType, "dayType must not be null");

    Map<MealTime, List<String>> source =
        (foodsByMealTime == null) ? Collections.emptyMap() : foodsByMealTime;
    Map<MealTime, List<String>> copy = new HashMap<>(source.size());

    for (Map.Entry<MealTime, List<String>> e : source.entrySet()) {
      List<String> list =
          (e.getValue() == null) ? Collections.emptyList() : List.copyOf(e.getValue());
      copy.put(e.getKey(), list);
    }

    this.foodsByMealTime = Collections.unmodifiableMap(copy);
    this.dangerousMealTimes =
        dangerousMealTimes == null ? Collections.emptySet() : Set.copyOf(dangerousMealTimes);
  }

  public static FoodEvaluation calculate(List<FoodRecord> records, DayType dayType) {
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

  public boolean isMealDangerous(MealTime mealTime) {
    return dangerousMealTimes.contains(mealTime);
  }
}
