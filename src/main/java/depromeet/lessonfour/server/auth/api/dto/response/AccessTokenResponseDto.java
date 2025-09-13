package depromeet.lessonfour.server.auth.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AccessTokenResponseDto(@JsonProperty("access_token") String accessToken) {}
