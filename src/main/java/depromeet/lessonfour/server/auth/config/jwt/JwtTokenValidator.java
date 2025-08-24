package depromeet.lessonfour.server.auth.config.jwt;

import java.util.UUID;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenValidator {

  private final JwtSecretKeyProvider secretKeyProvider;

  /** JWT 토큰 유효성 검증 */
  public boolean isValidToken(String token) {
    try {
      parseTokenClaims(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      log.debug("Invalid JWT token: {}", e.getMessage());
      return false;
    }
  }

  /** JWT 토큰에서 클레임 추출 */
  public Claims parseTokenClaims(String token) {
    return Jwts.parser()
        .verifyWith(secretKeyProvider.getSecretKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  /** JWT 토큰에서 사용자 ID 추출 */
  public UUID extractUserId(String token) {
    Claims claims = parseTokenClaims(token);
    Object userIdObject = claims.get("userId");
    if (userIdObject instanceof String) {
      return UUID.fromString((String) userIdObject);
    } else if (userIdObject instanceof UUID) {
      return (UUID) userIdObject;
    }
    throw new IllegalArgumentException("Invalid userId claim format");
  }

  /** JWT 토큰에서 이메일 추출 */
  public String extractEmail(String token) {
    Claims claims = parseTokenClaims(token);
    return claims.get("email", String.class);
  }

  /** JWT 토큰에서 닉네임 추출 */
  public String extractNickname(String token) {
    Claims claims = parseTokenClaims(token);
    return claims.get("nickname", String.class);
  }

  /** JWT 토큰에서 권한 정보 추출 */
  public String extractAuthority(String token) {
    Claims claims = parseTokenClaims(token);
    return claims.get("role", String.class);
  }

  /** JWT 토큰에서 Subject (사용자 ID) 추출 */
  public String extractSubject(String token) {
    Claims claims = parseTokenClaims(token);
    return claims.getSubject();
  }
}
