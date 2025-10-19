package depromeet.lessonfour.server.auth.app.service;

import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.auth.app.client.UserServiceClient;
import depromeet.lessonfour.server.auth.app.dto.response.TokenPairDto;
import depromeet.lessonfour.server.auth.domain.vo.UserInfo;
import depromeet.lessonfour.server.auth.infra.security.jwt.TokenManager;
import depromeet.lessonfour.server.auth.infra.security.oauth.OAuthTokenClient;
import depromeet.lessonfour.server.auth.infra.security.oauth.OidcTokenDecoder;
import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.user.domain.entity.User;
import depromeet.lessonfour.server.user.domain.vo.Provider;
import depromeet.lessonfour.server.user.domain.vo.Provider.ProviderType;
import lombok.RequiredArgsConstructor;

@UseCase
@Transactional
@RequiredArgsConstructor
public class OAuthCallbackUseCase {

  private final UserServiceClient userServiceClient;
  private final TokenManager tokenManager;
  private final OidcTokenDecoder oidcTokenDecoder;
  private final OAuthTokenClient oAuthTokenClient;

  public String login(String provider, String code) {
    ProviderType providerType = ProviderType.from(provider);
    OAuth2AccessTokenResponse response = oAuthTokenClient.requestToken(code, providerType);
    String idToken = (String) response.getAdditionalParameters().get("id_token");
    UserInfo info = oidcTokenDecoder.decode(idToken, provider);
    User user =
        userServiceClient.findOrCreate(
            info.getEmail(),
            info.getNickname(),
            info.getPicture(),
            Provider.of(providerType, info.getSub()));

    TokenPairDto tokenPair = tokenManager.generateAndStoreTokens(user, false);
    return tokenPair.refreshToken();
  }
}
