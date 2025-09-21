package depromeet.lessonfour.server.users.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.common.exception.ServerException;
import depromeet.lessonfour.server.common.exception.code.ErrorCode;
import depromeet.lessonfour.server.users.adapters.UserRepository;
import depromeet.lessonfour.server.users.domain.entities.User;
import depromeet.lessonfour.server.users.schemas.response.UserResponseDto;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserQueryService {

  private final UserRepository userRepository;

  public UserResponseDto findByEmail(String email) {
    User user = userRepository.findByEmail(email).orElseThrow(RuntimeException::new);

    return UserResponseDto.of(user);
  }

  public User findById(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new ServerException(ErrorCode.USER_NOT_FOUND));
  }
}
