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
    User existingUser = userRepository.findByEmail(email).orElse(null);
    boolean isNewUser = existingUser == null || existingUser.isDeleted();

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

    // 새로운 사용자인 경우 isNew를 true로 설정
    if (isNewUser) {
      user.setNew(true);
    }

    return user;
  }
}
