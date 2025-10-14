package depromeet.lessonfour.server.user.app.service;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.user.domain.entity.User;
import depromeet.lessonfour.server.user.domain.vo.Provider;
import depromeet.lessonfour.server.user.domain.vo.Provider.ProviderType;
import depromeet.lessonfour.server.user.infra.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  public User findOrCreate(
      String email, String nickname, String profileImage, String provider, String providerId) {
    return userRepository
        .findByEmail(email)
        .orElseGet(
            () -> {
              User newUser =
                  User.register(
                      email,
                      nickname,
                      null,
                      profileImage,
                      Provider.of(ProviderType.valueOf(provider), providerId));
              return userRepository.save(newUser);
            });
  }
}
