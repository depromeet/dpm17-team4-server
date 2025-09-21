package depromeet.lessonfour.server.user.app.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.user.app.dto.response.UserResponseDto;
import depromeet.lessonfour.server.user.domain.entity.User;
import depromeet.lessonfour.server.user.infra.repository.UserRepository;
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
}
