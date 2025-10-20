package depromeet.lessonfour.server.auth.app.service;

import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.user.domain.entity.User;
import depromeet.lessonfour.server.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@UseCase
@Transactional
@RequiredArgsConstructor
public class LogoutUseCase {

  private final UserRepository userRepository;

  public void logout(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    // Refresh token을 DB에서 제거하여 토큰 갱신 불가능하게 만듦
    user.storeRefreshToken(null);
    userRepository.save(user);
  }
}
