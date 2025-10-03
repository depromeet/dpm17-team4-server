package depromeet.lessonfour.server.user.app.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserUpdateService {

  private final UserRepository jpaUserRepository;

  public void updateRefreshToken(Long userId, String newRefreshToken) {
    jpaUserRepository
        .findById(userId)
        .ifPresent(
            user -> {
              user.storeRefreshToken(newRefreshToken);
            });
  }
}
