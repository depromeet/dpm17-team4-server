package depromeet.lessonfour.server.notification.app.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SaveNotificationSettingsRequestDto(
    @Schema(description = "기기 식별자", example = "my_iphone") @NotBlank(message = "기기 식별자는 필수입니다")
        String key,
    @Schema(description = "Firebase 등록 토큰", example = "fL8k9X...")
        @NotBlank(message = "등록 토큰은 필수입니다")
        String registrationToken,
    @Schema(description = "알림 활성화 여부", example = "true") @NotNull(message = "활성화 여부는 필수입니다") Boolean enabled) {}
