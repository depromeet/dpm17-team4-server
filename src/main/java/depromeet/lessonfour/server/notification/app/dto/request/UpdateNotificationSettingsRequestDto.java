package depromeet.lessonfour.server.notification.app.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record UpdateNotificationSettingsRequestDto(
    @Schema(description = "기기 식별자 (선택)", example = "my_iphone") String key,
    @Schema(description = "알림 활성화 여부 (선택)", example = "true") Boolean enabled) {}
