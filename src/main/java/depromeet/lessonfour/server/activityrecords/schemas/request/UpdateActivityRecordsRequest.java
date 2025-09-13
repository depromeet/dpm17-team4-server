package depromeet.lessonfour.server.activityrecords.schemas.request;

import java.util.List;
import java.util.UUID;

import depromeet.lessonfour.server.activityrecords.domain.entities.MealTime;
import depromeet.lessonfour.server.activityrecords.domain.entities.StressLevel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateActivityRecordsRequest(
    @Valid List<UpdateFoodRequestDto> foods,
    int waterIntakeCups,
    @NotNull StressLevel stressLevel) {

  public record UpdateFoodRequestDto(
      UUID id,
      @NotBlank(message = "음식 이름은 필수입니다") String name,
      @NotNull(message = "식사 시간은 필수입니다") MealTime mealTime) {}
}
