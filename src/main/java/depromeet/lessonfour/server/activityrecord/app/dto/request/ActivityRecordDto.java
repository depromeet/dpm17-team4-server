package depromeet.lessonfour.server.activityrecord.app.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import depromeet.lessonfour.server.activityrecord.domain.vo.MealTime;
import depromeet.lessonfour.server.activityrecord.domain.vo.StressLevel;

public record ActivityRecordDto(
    List<FoodDto> foods, Integer water, StressLevel stress, LocalDateTime occurredAt) {

  public record FoodDto(Long id, MealTime mealTime) {}
}
