package depromeet.lessonfour.server.user.app.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.user.domain.entity.User;
import depromeet.lessonfour.server.user.domain.repository.UserRepository;
import depromeet.lessonfour.server.user.domain.vo.Provider;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  @Transactional
  public User findOrCreate(String email, String nickname, String profileImage, Provider provider) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseGet(
                () -> {
                  User newUser = User.register(email, nickname, null, profileImage, provider);
                  return userRepository.save(newUser);
                });
    if (user.isDeleted()) {
      user.activate();
    }
    return user;
  }
}
