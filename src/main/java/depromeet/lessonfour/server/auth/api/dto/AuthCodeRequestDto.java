package depromeet.lessonfour.server.auth.api.dto;

import jakarta.validation.constraints.NotNull;

public record AuthCodeRequestDto(@NotNull(message = "auth code는 필수입니다.") String code) {}
