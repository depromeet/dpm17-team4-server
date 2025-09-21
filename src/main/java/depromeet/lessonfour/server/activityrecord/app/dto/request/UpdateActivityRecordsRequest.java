package depromeet.lessonfour.server.activityrecord.app.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import depromeet.lessonfour.server.activityrecord.domain.vo.MealTime;
import depromeet.lessonfour.server.activityrecord.domain.vo.StressLevel;
import depromeet.lessonfour.server.common.annotation.ValidEnum;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateActivityRecordsRequest(
    @Nullable @Valid List<UpdateFoodRequestDto> foods,
    @Nullable @Min(value = 0, message = "마신 물의 잔 수는 양수여야 합니다") Integer water,
    @Nullable @ValidEnum(enumClass = StressLevel.class, message = "유효하지 않은 스트레스 지수입니다")
        StressLevel stress,
    @Nullable @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS") LocalDateTime occurredAt) {

  public record UpdateFoodRequestDto(
      @NotNull(message = "음식 id는 필수입니다") Long id,
      @NotNull(message = "식사 시간은 필수입니다") @ValidEnum(enumClass = MealTime.class, message = "유효하지 않은 식사시간입니다.")
          MealTime mealTime) {}
}
