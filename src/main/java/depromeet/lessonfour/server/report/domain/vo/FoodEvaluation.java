package depromeet.lessonfour.server.report.domain.vo;

import java.util.List;
import java.util.Map;

import depromeet.lessonfour.server.activityrecord.domain.vo.MealTime;
import lombok.Getter;

@Getter
public class FoodEvaluation {

  private final DayType dayType;
  private final boolean dangerous;
  private final Map<MealTime, List<String>> foodsByMealTime;

  public FoodEvaluation(
      boolean dangerous, Map<MealTime, List<String>> foodsByMealTime, DayType dayType) {
    this.dangerous = dangerous;
    this.foodsByMealTime = foodsByMealTime;
    this.dayType = dayType;
  }
}
