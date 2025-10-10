package depromeet.lessonfour.server.auth.api.dto;

public record KakaoTokenRequestDto(String code, String redirectUri) {}
