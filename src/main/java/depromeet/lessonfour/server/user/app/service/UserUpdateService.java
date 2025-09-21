package depromeet.lessonfour.server.user.app.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.user.infra.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserUpdateService {

  private final UserRepository userRepository;

  public void updateRefreshToken(Long userId, String newRefreshToken) {
    userRepository
        .findById(userId)
        .ifPresent(
            user -> {
              user.storeRefreshToken(newRefreshToken);
            });
  }
}
