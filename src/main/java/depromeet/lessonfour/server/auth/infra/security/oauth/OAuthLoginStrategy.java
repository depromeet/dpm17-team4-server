package depromeet.lessonfour.server.auth.infra.security.oauth;

import org.springframework.security.oauth2.core.user.OAuth2User;

import depromeet.lessonfour.server.auth.app.dto.response.AuthResponseDto;
import depromeet.lessonfour.server.auth.domain.vo.SocialProvider;

public interface OAuthLoginStrategy {

  boolean supports(SocialProvider socialProvider);

  AuthResponseDto login(OAuth2User oAuth2User);
}
