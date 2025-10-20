package depromeet.lessonfour.server.notification.app.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record UpdateNotificationSettingsRequestDto(
    @Schema(description = "기기 식별자 (선택)", example = "my_iphone")
        @Size(max = 32, message = "기기 식별자는 최대 32자까지 가능합니다")
        String key,
    @Schema(description = "알림 활성화 여부 (선택)", example = "true") Boolean enabled) {}
