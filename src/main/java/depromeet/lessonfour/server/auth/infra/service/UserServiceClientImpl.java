package depromeet.lessonfour.server.auth.infra.service;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.auth.app.client.UserServiceClient;
import depromeet.lessonfour.server.auth.domain.vo.SocialProvider;
import depromeet.lessonfour.server.user.app.service.UserService;
import depromeet.lessonfour.server.user.domain.entity.User;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserServiceClientImpl implements UserServiceClient {

  private final UserService userService;

  @Override
  public User findOrCreate(
      String email,
      String nickname,
      String profileImage,
      SocialProvider socialProvider,
      String providerId) {
    return userService.findOrCreate(
        email, nickname, profileImage, socialProvider.toProvider(providerId));
  }
}
