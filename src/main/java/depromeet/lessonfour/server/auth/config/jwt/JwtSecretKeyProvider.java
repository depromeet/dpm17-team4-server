package depromeet.lessonfour.server.auth.config.jwt;

import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Getter
@Component
public class JwtSecretKeyProvider {

  @Value("${jwt.secret:myDefaultSecretKeyForJWTWhichShouldBeAtLeast256BitsLong}")
  private String secretKeyString;

  private SecretKey secretKey;

  @PostConstruct
  void initializeSecretKey() {
    this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
  }
}
