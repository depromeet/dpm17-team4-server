package depromeet.lessonfour.server.users.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.users.adapters.UserRepository;
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
