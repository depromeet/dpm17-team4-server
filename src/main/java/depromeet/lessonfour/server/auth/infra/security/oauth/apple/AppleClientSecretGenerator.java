package depromeet.lessonfour.server.auth.infra.security.oauth.apple;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AppleClientSecretGenerator {

  @Value("${apple.team-id}")
  private String teamId;

  @Value("${apple.key-id}")
  private String keyId;

  @Value("${spring.security.oauth2.client.registration.apple.client-id}")
  private String clientId;

  @Value("${apple.private-key}")
  private String privateKeyPem;

  public String generate(String clientId, String tokenUri) {
    try {
      PrivateKey privateKey = parsePrivateKey(privateKeyPem);

      Instant now = Instant.now();
      Instant exp = now.plusSeconds(60 * 5);

      return Jwts.builder()
          .header()
          .keyId(keyId)
          .and()
          .issuer(teamId)
          .issuedAt(Date.from(now))
          .expiration(Date.from(exp))
          .audience()
          .add("https://appleid.apple.com")
          .and()
          .subject(clientId)
          .signWith(privateKey)
          .compact();
    } catch (Exception e) {
      throw new IllegalStateException("Failed to generate Apple client secret", e);
    }
  }

  private PrivateKey parsePrivateKey(String pem) throws Exception {
    String cleanPem =
        pem.replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\r", "")
            .replace("\\n", "")
            .replaceAll("\\r", "")
            .replaceAll("\\n", "")
            .replaceAll("\\s+", "");
    byte[] decoded = Base64.getDecoder().decode(cleanPem);
    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
    return KeyFactory.getInstance("EC").generatePrivate(spec);
  }
}
