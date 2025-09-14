package depromeet.lessonfour.server.activityrecords.schemas.request;

import java.util.List;

import depromeet.lessonfour.server.activityrecords.domain.entities.MealTime;
import depromeet.lessonfour.server.activityrecords.domain.entities.StressLevel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateActivityRecordsRequest(
    @Valid List<CreateFoodRequestDto> foods,
    @Min(0) int waterIntakeCups,
    @NotNull StressLevel stressLevel) {

  public record CreateFoodRequestDto(
      @NotBlank(message = "음식 이름은 필수입니다") String name,
      @NotNull(message = "식사 시간은 필수입니다") MealTime mealTime) {}
}
