package depromeet.lessonfour.server.activityrecord.api.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import depromeet.lessonfour.server.activityrecord.app.dto.request.ActivityRecordDto;
import depromeet.lessonfour.server.activityrecord.app.dto.request.ActivityRecordDto.FoodDto;
import depromeet.lessonfour.server.activityrecord.domain.vo.MealTime;
import depromeet.lessonfour.server.activityrecord.domain.vo.StressLevel;
import depromeet.lessonfour.server.common.annotation.ValidEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateActivityRecordsRequest(
    @Valid List<CreateFoodRequestDto> foods,
    @PositiveOrZero(message = "마신 물의 잔 수는 0 이상이어야 합니다") Integer water,
    @ValidEnum(enumClass = StressLevel.class, message = "유효하지 않은 스트레스 지수입니다") StressLevel stress,
    @NotNull(message = "선택한 날짜와 시간은 필수입니다") @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        LocalDateTime occurredAt) {

  public record CreateFoodRequestDto(
      @NotNull(message = "음식 id는 필수입니다") Long id,
      @NotNull(message = "식사 시간은 필수입니다") @ValidEnum(enumClass = MealTime.class, message = "유효하지 않은 식사시간입니다")
          MealTime mealTime) {}

  public ActivityRecordDto to() {
    List<FoodDto> foods = null;
    if (this.foods != null) {
      foods = this.foods.stream().map(fr -> new FoodDto(fr.id(), fr.mealTime())).toList();
    }
    return new ActivityRecordDto(foods, this.water, this.stress, this.occurredAt);
  }
}
