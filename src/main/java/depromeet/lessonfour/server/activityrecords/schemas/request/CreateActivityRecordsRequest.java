package depromeet.lessonfour.server.activityrecords.schemas.request;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import depromeet.lessonfour.server.activityrecords.domain.entities.MealTime;
import depromeet.lessonfour.server.activityrecords.domain.entities.StressLevel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateActivityRecordsRequest(
    @Valid List<CreateFoodRequestDto> selectedFoods,
    @Min(value = 0, message = "마신 물의 잔 수는 양수여야 합니다") int selectedWater,
    @NotNull(message = "스트레스 지수는 필수입니다") StressLevel selectedStress,
    @NotNull(message = "선택한 날짜와 시간은 필수입니다") @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
        LocalDateTime selectedWhen) {

  public record CreateFoodRequestDto(
      @NotNull(message = "음식 id는 필수입니다") Long foodId,
      @NotNull(message = "식사 시간은 필수입니다") MealTime time) {}
}
