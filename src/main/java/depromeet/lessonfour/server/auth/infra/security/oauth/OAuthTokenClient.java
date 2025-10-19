package depromeet.lessonfour.server.auth.infra.security.oauth;

import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.auth.api.code.AuthErrorCode;
import depromeet.lessonfour.server.auth.infra.security.oauth.apple.AppleTokenClient;
import depromeet.lessonfour.server.auth.infra.security.oauth.kakao.KakaoTokenClient;
import depromeet.lessonfour.server.common.exception.ServerException;
import depromeet.lessonfour.server.user.domain.vo.Provider;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuthTokenClient {

  private final KakaoTokenClient kakaoTokenClient;
  private final AppleTokenClient appleTokenClient;

  public OAuth2AccessTokenResponse requestToken(String code, Provider.ProviderType providerType) {
    return switch (providerType) {
      case KAKAO -> kakaoTokenClient.token(code);
      case APPLE -> appleTokenClient.token(code);
      case LOCAL -> throw new ServerException(AuthErrorCode.OAUTH_PROVIDER_NOT_FOUND);
    };
  }
}
