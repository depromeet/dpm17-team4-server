package depromeet.lessonfour.server.user.app.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.common.api.code.ErrorCode;
import depromeet.lessonfour.server.common.exception.ServerException;
import depromeet.lessonfour.server.user.domain.entity.User;
import depromeet.lessonfour.server.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserQueryService {

  private final UserRepository userRepository;

  public User getUserByEmail(String email) {
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new ServerException(ErrorCode.USER_NOT_FOUND));
  }

  public User getActivatedUserById(Long userId) {
    return userRepository
        .findActiveById(userId)
        .orElseThrow(() -> new ServerException(ErrorCode.USER_NOT_FOUND));
  }
}
