package depromeet.lessonfour.server.auth.infra.security.oauth.kakao;

import static depromeet.lessonfour.server.auth.domain.vo.SocialProvider.KAKAO;

import java.util.Map;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.auth.app.client.UserServiceClient;
import depromeet.lessonfour.server.auth.app.dto.response.AuthResponseDto;
import depromeet.lessonfour.server.auth.domain.vo.SocialProvider;
import depromeet.lessonfour.server.auth.infra.security.oauth.OAuthLoginStrategy;
import depromeet.lessonfour.server.user.domain.entity.User;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KakaoLoginStrategy implements OAuthLoginStrategy {

  private final UserServiceClient userServiceClient;

  @Override
  public boolean supports(SocialProvider socialProvider) {
    return KAKAO.equals(socialProvider);
  }

  @Override
  public AuthResponseDto login(OAuth2User oAuth2User) {
    Map<String, Object> attributes = oAuth2User.getAttributes();

    String sub = String.valueOf(oAuth2User.getAttributes().get("id"));
    String email = (String) attributes.get("email");
    String nickname = (String) attributes.get("nickname");
    String profileImage = (String) attributes.get("profileImage");

    User user = userServiceClient.findOrCreate(email, nickname, profileImage, KAKAO, sub);

    return AuthResponseDto.of(user);
  }
}
