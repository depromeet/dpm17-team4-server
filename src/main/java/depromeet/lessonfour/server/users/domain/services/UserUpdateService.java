package depromeet.lessonfour.server.users.domain.services;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.users.domain.ports.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserUpdateService {

  private final UserRepository userRepository;

  public void updateRefreshToken(UUID userId, String newRefreshToken) {
    userRepository
        .findById(userId)
        .ifPresent(
            user -> {
              user.storeRefreshToken(newRefreshToken);
            });
  }
}
