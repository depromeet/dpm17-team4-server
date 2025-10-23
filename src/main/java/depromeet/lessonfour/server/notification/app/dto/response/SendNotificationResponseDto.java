package depromeet.lessonfour.server.notification.app.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record SendNotificationResponseDto(
    @Schema(description = "성공 토큰 수") int successCount,
    @Schema(description = "실패 토큰 수") int failureCount) {}
