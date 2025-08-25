package depromeet.lessonfour.server.auth.config.jwt;

import java.time.Duration;

public interface JwtConstants {

    Duration REFRESH_TOKEN_EXPIRATION = Duration.ofDays(7);
    String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    String BEARER_PREFIX = "Bearer ";
    String AUTHORIZATION_HEADER = "Authorization";
}
