package depromeet.lessonfour.server.auth.infra.security.oauth;

import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.auth.api.code.AuthErrorCode;
import depromeet.lessonfour.server.auth.domain.vo.SocialProvider;
import depromeet.lessonfour.server.auth.infra.security.oauth.kakao.KakaoOAuthTokenClient;
import depromeet.lessonfour.server.common.exception.ServerException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuthTokenClient {

  private final KakaoOAuthTokenClient kakaoOAuthTokenClient;

  public OAuth2AccessTokenResponse requestToken(String code, SocialProvider socialProvider) {
    return switch (socialProvider) {
      case KAKAO -> kakaoOAuthTokenClient.token(code);
      default -> throw new ServerException(AuthErrorCode.OAUTH_PROVIDER_NOT_FOUND);
    };
  }
}
