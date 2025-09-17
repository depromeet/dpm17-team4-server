package depromeet.lessonfour.server.activityrecords.schemas.response;

import java.time.LocalDateTime;
import java.util.List;

import depromeet.lessonfour.server.activityrecords.domain.entities.MealTime;
import depromeet.lessonfour.server.activityrecords.domain.entities.StressLevel;

public record GetActivityRecordsResponse(
    Long id,
    int waterIntakeCups,
    StressLevel stressLevel,
    List<FoodResponse> foods,
    LocalDateTime createdAt) {

  public record FoodResponse(Long id, String name, MealTime mealTime) {}
}
