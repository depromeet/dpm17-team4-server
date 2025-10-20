package depromeet.lessonfour.server.notification.app.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SaveNotificationSettingResponseDto(
    @Schema(description = "알림 설정 ID") Long id,
    @Schema(description = "사용자 ID") Long userId,
    @Schema(description = "Firebase 등록 토큰") String registrationToken,
    @Schema(description = "알림 활성화 여부") Boolean enabled) {}
