package depromeet.lessonfour.server.auth.config.jwt;

import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtSecretKeyProvider {

  @Getter(AccessLevel.NONE)
  @Value("${jwt.secret:myDefaultSecretKeyForJWTWhichShouldBeAtLeast256BitsLong}")
  private String secretKeyString;

  @Getter
  private SecretKey secretKey;

  @PostConstruct
  void initializeSecretKey() {
    this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
  }
}
