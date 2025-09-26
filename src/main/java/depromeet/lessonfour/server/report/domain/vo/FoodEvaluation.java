package depromeet.lessonfour.server.report.domain.vo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import depromeet.lessonfour.server.activityrecord.domain.vo.MealTime;
import lombok.Getter;

@Getter
public class FoodEvaluation {

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
          (e.getValue() == null)
              ? Collections.emptyList()
              : Collections.unmodifiableList(new ArrayList<>(e.getValue()));
      copy.put(e.getKey(), list);
    }
    this.foodsByMealTime = Collections.unmodifiableMap(copy);
    this.dangerousMealTimes =
        dangerousMealTimes == null ? Collections.emptySet() : Set.copyOf(dangerousMealTimes);
  }

  public boolean isMealDangerous(MealTime mealTime) {
    return dangerousMealTimes.contains(mealTime);
  }
}
