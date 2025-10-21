package depromeet.lessonfour.server.activityrecord.api.dto.request;

import java.util.List;

import depromeet.lessonfour.server.activityrecord.app.dto.request.ActivityRecordDto;
import depromeet.lessonfour.server.activityrecord.app.dto.request.ActivityRecordDto.FoodDto;
import depromeet.lessonfour.server.activityrecord.domain.vo.MealTime;
import depromeet.lessonfour.server.activityrecord.domain.vo.StressLevel;
import depromeet.lessonfour.server.common.annotation.ValidEnum;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateActivityRecordsRequest(
    @Nullable @Valid List<UpdateFoodRequestDto> foods,
    @Nullable @PositiveOrZero(message = "마신 물의 잔 수는 0 이상이어야 합니다") Integer water,
    @Nullable @ValidEnum(enumClass = StressLevel.class, message = "유효하지 않은 스트레스 지수입니다")
        StressLevel stress) {

  public record UpdateFoodRequestDto(
      @NotNull(message = "음식 id는 필수입니다") Long id,
      @NotNull(message = "식사 시간은 필수입니다") @ValidEnum(enumClass = MealTime.class, message = "유효하지 않은 식사시간입니다.")
          MealTime mealTime) {}

  public ActivityRecordDto to() {
    List<FoodDto> foods = null;
    if (this.foods != null) {
      foods = this.foods.stream().map(fr -> new FoodDto(fr.id(), fr.mealTime())).toList();
    }

    return new ActivityRecordDto(foods, this.water, this.stress, null);
  }
}
