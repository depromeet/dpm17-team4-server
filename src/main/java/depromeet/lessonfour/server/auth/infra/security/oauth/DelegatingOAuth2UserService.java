package depromeet.lessonfour.server.auth.infra.security.oauth;

import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.auth.infra.security.oauth.kakao.KakaoOAuth2UserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DelegatingOAuth2UserService
    implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

  private final KakaoOAuth2UserService kakaoService;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) {
    String registrationId = userRequest.getClientRegistration().getRegistrationId();
    return switch (registrationId) {
      case "kakao" -> kakaoService.loadUser(userRequest);
      case "apple" -> throw new OAuth2AuthenticationException("Apple login not supported yet");
      default -> throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
    };
  }
}
