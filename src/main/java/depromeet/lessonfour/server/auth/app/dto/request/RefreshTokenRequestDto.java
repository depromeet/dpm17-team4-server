package depromeet.lessonfour.server.auth.app.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDto(@NotBlank String refreshToken) {}
