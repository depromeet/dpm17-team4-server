package depromeet.lessonfour.server.activityrecords.schemas.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import depromeet.lessonfour.server.activityrecords.domain.entities.MealTime;
import depromeet.lessonfour.server.activityrecords.domain.entities.StressLevel;

public record GetActivityRecordsResponse(
    UUID id,
    int waterIntakeCups,
    StressLevel stressLevel,
    List<FoodResponse> foods,
    LocalDateTime createdAt) {

  public record FoodResponse(UUID id, String name, MealTime mealTime) {}
}
