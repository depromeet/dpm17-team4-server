package depromeet.lessonfour.server.auth.infra.service;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.auth.app.client.UserServiceClient;
import depromeet.lessonfour.server.user.app.service.UserService;
import depromeet.lessonfour.server.user.domain.entity.User;
import depromeet.lessonfour.server.user.domain.vo.Provider;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserServiceClientImpl implements UserServiceClient {

  private final UserService userService;

  @Override
  public User findOrCreate(String email, String nickname, String profileImage, Provider provider) {
    return userService.findOrCreate(email, nickname, profileImage, provider);
  }
}
