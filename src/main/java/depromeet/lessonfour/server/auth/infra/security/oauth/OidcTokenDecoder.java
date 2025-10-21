package depromeet.lessonfour.server.auth.infra.security.oauth;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.auth.api.code.AuthErrorCode;
import depromeet.lessonfour.server.auth.domain.vo.UserInfo;
import depromeet.lessonfour.server.common.exception.ServerException;

@Component
public class OidcTokenDecoder {

  private final Map<String, JwtDecoder> decoders = new ConcurrentHashMap<>();

  @Value("${spring.security.oauth2.client.provider.kakao.jwk-set-uri}")
  private String kakaoJwkSetUri;

  @Value("${spring.security.oauth2.client.provider.kakao.issuer-uri}")
  private String kakaoIssuerUri;

  @Value("${spring.security.oauth2.client.provider.apple.jwk-set-uri}")
  private String appleJwkSetUri;

  @Value("${spring.security.oauth2.client.provider.apple.issuer-uri}")
  private String appleIssuerUri;

  public UserInfo decode(String token, String provider) {
    try {
      JwtDecoder decoder = getDecoder(provider);
      Jwt jwt = decoder.decode(token);
      return UserInfo.from(jwt.getClaims());
    } catch (Exception e) {
      throw new ServerException(AuthErrorCode.INVALID_OIDC_TOKEN);
    }
  }

  private JwtDecoder getDecoder(String provider) {
    return decoders.computeIfAbsent(provider.toLowerCase(), this::createDecoder);
  }

  private JwtDecoder createDecoder(String provider) {
    String jwkSetUri;
    String issuerUri;

    switch (provider) {
      case "kakao":
        jwkSetUri = kakaoJwkSetUri;
        issuerUri = kakaoIssuerUri;
        break;
      case "apple":
        jwkSetUri = appleJwkSetUri;
        issuerUri = appleIssuerUri;
        break;
      default:
        throw new ServerException(AuthErrorCode.INVALID_OIDC_TOKEN);
    }

    NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    OAuth2TokenValidator<Jwt> validator = JwtValidators.createDefaultWithIssuer(issuerUri);
    decoder.setJwtValidator(validator);

    return decoder;
  }
}
