package depromeet.lessonfour.server.user.app.service;

import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.common.api.code.ErrorCode;
import depromeet.lessonfour.server.common.exception.ServerException;
import depromeet.lessonfour.server.user.domain.User;
import depromeet.lessonfour.server.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;

@UseCase
@Transactional
@RequiredArgsConstructor
public class UserDeleteUseCase {

  private final UserRepository userRepository;

  public void delete(Long userId) {
    User user =
        userRepository
            .findActiveById(userId)
            .orElseThrow(() -> new ServerException(ErrorCode.USER_NOT_FOUND));
    user.deactivate();
  }
}
