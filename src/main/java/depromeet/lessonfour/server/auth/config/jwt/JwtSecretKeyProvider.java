package depromeet.lessonfour.server.auth.config.jwt;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Getter;

@Component
public class JwtSecretKeyProvider {

  @Getter(AccessLevel.NONE)
  @Value("${jwt.secret}")
  private String secretKeyString;

  @Getter private SecretKey secretKey;

  @PostConstruct
  void initializeSecretKey() {
    if (secretKeyString == null || secretKeyString.isBlank()) {
      throw new IllegalStateException("Missing required property 'jwt.secret'.");
    }
    try {
      this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    } catch (IllegalArgumentException e) {
      throw new IllegalStateException(
          "Invalid 'jwt.secret': HMAC key must be at least 256 bits.", e);
    } finally {
      // 힙 메모리에서 key값 제거
      this.secretKeyString = null;
    }
  }
}
