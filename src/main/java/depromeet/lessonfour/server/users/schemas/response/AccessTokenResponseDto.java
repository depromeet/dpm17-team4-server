package depromeet.lessonfour.server.users.schemas.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AccessTokenResponseDto(@JsonProperty("access_token") String accessToken) {}
