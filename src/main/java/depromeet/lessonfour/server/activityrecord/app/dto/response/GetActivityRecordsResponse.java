package depromeet.lessonfour.server.activityrecord.app.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import depromeet.lessonfour.server.activityrecord.domain.entity.FoodRecord;
import depromeet.lessonfour.server.activityrecord.domain.vo.ActivityAt;
import depromeet.lessonfour.server.activityrecord.domain.vo.MealTime;
import depromeet.lessonfour.server.activityrecord.domain.vo.StressLevel;

public record GetActivityRecordsResponse(
    Long id,
    int waterIntakeCups,
    StressLevel stressLevel,
    List<FoodResponse> foods,
    LocalDateTime occurredAt) {

  public record FoodResponse(Long id, String name, MealTime mealTime) {}

  public static GetActivityRecordsResponse from(
      Long id,
      int waterIntakeCups,
      StressLevel stressLevel,
      List<FoodRecord> foodRecords,
      ActivityAt occurredAt) {
    List<FoodResponse> foods =
        foodRecords.stream()
            .map(
                fr ->
                    new FoodResponse(
                        fr.getFood().getId(), fr.getFood().getName(), fr.getMealTime()))
            .toList();
    return new GetActivityRecordsResponse(
        id, waterIntakeCups, stressLevel, foods, occurredAt.toDateTime());
  }
}
