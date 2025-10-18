package depromeet.lessonfour.server.auth.infra.security.oauth;

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

  private final JwtDecoder jwtDecoder;

  public OidcTokenDecoder(
      @Value("${spring.security.oauth2.client.provider.kakao.jwk-set-uri}") String jwkSetUri,
      @Value("${spring.security.oauth2.client.provider.kakao.issuer-uri}") String issuerUri) {

    NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

    OAuth2TokenValidator<Jwt> validator = JwtValidators.createDefaultWithIssuer(issuerUri);
    decoder.setJwtValidator(validator);

    this.jwtDecoder = decoder;
  }

  public UserInfo decode(String token) {
    try {
      Jwt jwt = jwtDecoder.decode(token);
      return UserInfo.from(jwt.getClaims());
    } catch (Exception e) {
      throw new ServerException(AuthErrorCode.INVALID_OIDC_TOKEN);
    }
  }
}
