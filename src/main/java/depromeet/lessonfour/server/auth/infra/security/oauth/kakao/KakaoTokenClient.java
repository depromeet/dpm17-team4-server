package depromeet.lessonfour.server.auth.infra.security.oauth.kakao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.auth.api.code.AuthErrorCode;
import depromeet.lessonfour.server.common.exception.ServerException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KakaoTokenClient {
  @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
  private String kakaoRedirectUri;

  private final ClientRegistrationRepository clientRegistrationRepository;

  public OAuth2AccessTokenResponse token(String code) {
    ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("kakao");
    if (registration == null) {
      throw new ServerException(AuthErrorCode.OAUTH_PROVIDER_NOT_FOUND);
    }

    OAuth2AuthorizationRequest authRequest =
        OAuth2AuthorizationRequest.authorizationCode()
            .authorizationUri(registration.getProviderDetails().getAuthorizationUri())
            .clientId(registration.getClientId())
            .redirectUri(kakaoRedirectUri)
            .build();

    OAuth2AuthorizationResponse authResponse =
        OAuth2AuthorizationResponse.success(code).redirectUri(kakaoRedirectUri).build();

    return getOAuth2AccessTokenResponse(authRequest, authResponse, registration);
  }

  private OAuth2AccessTokenResponse getOAuth2AccessTokenResponse(
      OAuth2AuthorizationRequest authRequest,
      OAuth2AuthorizationResponse authResponse,
      ClientRegistration registration) {
    OAuth2AuthorizationExchange exchange =
        new OAuth2AuthorizationExchange(authRequest, authResponse);
    OAuth2AuthorizationCodeGrantRequest grantRequest =
        new OAuth2AuthorizationCodeGrantRequest(registration, exchange);

    RestClientAuthorizationCodeTokenResponseClient tokenClient =
        new RestClientAuthorizationCodeTokenResponseClient();
    OAuth2AccessTokenResponse tokenResponse = tokenClient.getTokenResponse(grantRequest);

    if (!tokenResponse.getAdditionalParameters().containsKey("id_token")) {
      throw new ServerException(AuthErrorCode.ID_TOKEN_REQUIRED);
    }
    return tokenResponse;
  }
}
