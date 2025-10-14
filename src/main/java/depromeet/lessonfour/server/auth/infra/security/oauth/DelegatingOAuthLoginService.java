package depromeet.lessonfour.server.auth.infra.security.oauth;

import java.util.List;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.auth.api.code.AuthErrorCode;
import depromeet.lessonfour.server.auth.app.dto.response.AuthResponseDto;
import depromeet.lessonfour.server.auth.domain.vo.SocialProvider;
import depromeet.lessonfour.server.common.exception.ServerException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DelegatingOAuthLoginService {

  private final List<OAuthLoginStrategy> strategies;

  public AuthResponseDto login(String provider, OAuth2User oAuth2User) {
    OAuthLoginStrategy processor = getLoginProvider(provider);
    return processor.login(oAuth2User);
  }

  private OAuthLoginStrategy getLoginProvider(String provider) {
    return strategies.stream()
        .filter(s -> s.supports(SocialProvider.from(provider)))
        .findFirst()
        .orElseThrow(() -> new ServerException(AuthErrorCode.OAUTH_PROVIDER_NOT_FOUND));
  }
}
