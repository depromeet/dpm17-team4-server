package depromeet.lessonfour.server.toiletrecords.schemas.request;

import java.time.LocalDateTime;

import depromeet.lessonfour.server.common.annotation.ValidEnum;
import depromeet.lessonfour.server.toiletrecords.domain.entities.ToiletColor;
import depromeet.lessonfour.server.toiletrecords.domain.entities.ToiletShape;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ToiletRecordCreateRequestDto(
    @NotNull(message = "배변시각 기록은 필수입니다.") LocalDateTime toiletAt,
    @NotNull(message = "배변 여부 기록은 필수입니다.") Boolean isToiletSuccess,
    @ValidEnum(enumClass = ToiletColor.class, message = "유효하지 않은 배변 색깔입니다.")
        ToiletColor toiletColor,
    @ValidEnum(enumClass = ToiletShape.class, message = "유효하지 않은 배변 형태입니다.")
        ToiletShape toiletShape,
    @Min(value = 0, message = "복통 점수는 0 이상이어야 합니다.")
        @Max(value = 100, message = "복통 점수는 100 이하여야 합니다.")
        int painScore,
    @Min(value = 5, message = "소요시간은 5 이상이어야 합니다.") @Max(value = 15, message = "소요시간은 15 이하여야 합니다.")
        int toiletDuration,
    @Nullable String additionalNote) {}
