package depromeet.lessonfour.server.activityrecord.app.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import depromeet.lessonfour.server.activityrecord.domain.vo.MealTime;
import depromeet.lessonfour.server.activityrecord.domain.vo.StressLevel;

public record GetActivityRecordsResponse(
    Long id,
    int waterIntakeCups,
    StressLevel stressLevel,
    List<FoodResponse> foods,
    LocalDateTime createdAt) {

  public record FoodResponse(Long id, String name, MealTime mealTime) {}
}
