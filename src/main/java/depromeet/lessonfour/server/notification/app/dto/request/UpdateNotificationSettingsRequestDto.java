package depromeet.lessonfour.server.notification.app.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record UpdateNotificationSettingsRequestDto(
    @Schema(description = "알림 활성화 여부", example = "true") @NotNull(message = "활성화 여부는 필수입니다") Boolean enabled) {}
