package depromeet.lessonfour.server.auth.api.util;

import static depromeet.lessonfour.server.auth.infra.security.jwt.JwtConstants.REFRESH_TOKEN_COOKIE_NAME;

import org.springframework.http.ResponseCookie;

public class RefreshTokenCookieGenerator {
  public static ResponseCookie generate(String refreshToken) {
    return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
        .httpOnly(false)
        .sameSite("None")
        .maxAge(7 * 24 * 60 * 60)
        .path("/")
        .secure(true)
        .build();
  }

  public static ResponseCookie generate(String refreshToken, String domain) {
    ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
        .httpOnly(false)
        .sameSite("None")
        .maxAge(7 * 24 * 60 * 60)
        .path("/")
        .secure(true);
    
    if (domain != null && !domain.isBlank()) {
      builder.domain(domain);
    }
    
    return builder.build();
  }
}
