package depromeet.lessonfour.server.auth.infra.security.jwt;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.auth.domain.vo.AccountContext;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtTokenGenerator {

  private final JwtSecretKeyProvider secretKeyProvider;

  @Value("${jwt.expiration}")
  private Long accessTokenExpirationSeconds;

  @Value("${jwt.refresh-expiration}")
  private Long refreshTokenExpirationSeconds;

  public String generateAccessToken(AccountContext accountContext) {
    long expirationMillis = accessTokenExpirationSeconds * 1000L;
    return Jwts.builder()
        .subject(String.valueOf(accountContext.getId()))
        .id(UUID.randomUUID().toString())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + expirationMillis))
        .claim("email", accountContext.getEmail())
        .claim("nickname", accountContext.getNickname())
        .signWith(secretKeyProvider.getSecretKey())
        .compact();
  }

  public String generateRefreshToken(AccountContext accountContext) {
    long expirationMillis = refreshTokenExpirationSeconds * 1000L;
    return Jwts.builder()
        .subject(String.valueOf(accountContext.getId()))
        .id(UUID.randomUUID().toString()) // jti
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + expirationMillis))
        .signWith(secretKeyProvider.getSecretKey())
        .compact();
  }

  // 테스트용 오버로드 메서드들
  public String generateAccessToken(Long userId, String email, String nickname) {
    long expirationMillis = accessTokenExpirationSeconds * 1000L;
    return Jwts.builder()
        .subject(String.valueOf(userId))
        .id(UUID.randomUUID().toString())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + expirationMillis))
        .claim("email", email)
        .claim("nickname", nickname)
        .signWith(secretKeyProvider.getSecretKey())
        .compact();
  }

  public String generateAccessToken(
      Long userId, String email, String nickname, Instant expiration) {
    return Jwts.builder()
        .subject(String.valueOf(userId))
        .id(UUID.randomUUID().toString())
        .issuedAt(new Date())
        .expiration(Date.from(expiration))
        .claim("email", email)
        .claim("nickname", nickname)
        .signWith(secretKeyProvider.getSecretKey())
        .compact();
  }
}
