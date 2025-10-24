package depromeet.lessonfour.server.notification.app.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record SendNotificationRequestDto(
    @Schema(description = "알림 제목", example = "새로운 메시지") @NotBlank(message = "제목은 필수입니다")
        String title,
    @Schema(description = "알림 내용", example = "안녕하세요!") @NotBlank(message = "내용은 필수입니다")
        String body) {}
