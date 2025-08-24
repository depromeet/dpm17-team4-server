package depromeet.lessonfour.server.auth.config.jwt;

import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.auth.persist.jpa.entity.User;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtTokenGenerator {

  private final JwtSecretKeyProvider secretKeyProvider;

  @Value("${jwt.expiration:86400}")
  private Long accessTokenExpirationSeconds;

  private static final long REFRESH_TOKEN_MULTIPLIER = 7L; // 7 days

  public String generateAccessToken(User user) {
    long expirationMillis = accessTokenExpirationSeconds * 1000L;
    return Jwts.builder()
        .subject(String.valueOf(user.getId()))
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + expirationMillis))
        .claim("role", user.getAuthority())
        .claim("email", user.getEmail())
        .claim("nickname", user.getNickname())
        .claim("userId", user.getId().toString())
        .signWith(secretKeyProvider.getSecretKey())
        .compact();
  }

  public String generateRefreshToken(User user) {
    long expirationMillis = accessTokenExpirationSeconds * 1000L * REFRESH_TOKEN_MULTIPLIER;
    return Jwts.builder()
        .subject(String.valueOf(user.getId()))
        .id(UUID.randomUUID().toString()) // jti
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + expirationMillis))
        .signWith(secretKeyProvider.getSecretKey())
        .compact();
  }
}
