package depromeet.lessonfour.server.auth.config.jwt;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {

  private final JwtTokenValidator jwtTokenValidator;

  /**
   * JWT 토큰 유효성 검증
   *
   * @param jwt 검증할 JWT 토큰
   * @return 토큰이 유효하면 true, 그렇지 않으면 false
   */
  public boolean validateToken(String jwt) {
    if (jwt == null || jwt.trim().isEmpty()) {
      log.debug("JWT token is null or empty");
      return false;
    }

    try {
      return jwtTokenValidator.isValidToken(jwt);
    } catch (Exception e) {
      log.debug("JWT token validation failed: {}", e.getMessage());
      return false;
    }
  }

  /**
   * JWT 토큰에서 이메일 추출
   *
   * @param jwt JWT 토큰
   * @return 토큰에서 추출한 이메일, 추출 실패 시 null
   */
  public String extractEmail(String jwt) {
    if (jwt == null || jwt.trim().isEmpty()) {
      log.debug("JWT token is null or empty");
      return null;
    }

    try {
      return jwtTokenValidator.extractEmail(jwt);
    } catch (Exception e) {
      log.debug("Failed to extract email from JWT token: {}", e.getMessage());
      return null;
    }
  }
}
