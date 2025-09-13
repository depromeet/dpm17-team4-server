package depromeet.lessonfour.server.auth.security.jwt;

import java.time.Duration;

public interface JwtConstants {

  Duration REFRESH_TOKEN_EXPIRATION = Duration.ofDays(7);
  String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
  String BEARER_PREFIX = "Bearer ";
  String AUTHORIZATION_HEADER = "Authorization";
}
