package depromeet.lessonfour.server.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.auth.api.dto.response.UserResponseDto;
import depromeet.lessonfour.server.auth.persist.jpa.UserRepository;
import depromeet.lessonfour.server.auth.persist.jpa.entity.User;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserQueryService {

  private final UserRepository userRepository;

  public UserResponseDto findByEmail(String email) {
    User user = userRepository.findByEmail(email).orElseThrow(RuntimeException::new);

    return new UserResponseDto(
        user.getId(),
        user.getEmail(),
        user.getPassword(),
        user.getNickname(),
        user.getRole(),
        user.getProvider(),
        user.getProviderUserId());
  }
}
