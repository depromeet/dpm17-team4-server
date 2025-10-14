package depromeet.lessonfour.server.auth.infra.security.oauth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.auth.domain.vo.SocialProvider;

@Component
public class RedirectUriResolver {

  @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
  private String kakaoRedirectUri;

  private final String appleRedirectUri = null;

  public String getRedirectUri(SocialProvider socialProvider) {
    return switch (socialProvider) {
      case KAKAO -> kakaoRedirectUri;
      case APPLE -> null;
    };
  }
}
