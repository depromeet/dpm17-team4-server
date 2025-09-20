package depromeet.lessonfour.server.auth.security.jwt;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.common.annotation.Value;
import io.jsonwebtoken.security.Keys;
import lombok.AccessLevel;
import lombok.Getter;

@Component
@DependsOn({"valueProcessor"})
public class JwtSecretKeyProvider {

  @Getter(AccessLevel.NONE)
  @Value("${jwt.secret}")
  private String secretKeyString;

  private SecretKey secretKey;
  private boolean initialized = false;

  public SecretKey getSecretKey() {
    if (!initialized) {
      initializeSecretKey();
    }
    return secretKey;
  }

  private synchronized void initializeSecretKey() {
    if (initialized) {
      return;
    }

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
      this.initialized = true;
    }
  }
}
