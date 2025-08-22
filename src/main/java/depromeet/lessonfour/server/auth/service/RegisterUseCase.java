package depromeet.lessonfour.server.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.auth.api.dto.request.RegisterRequestDto;
import depromeet.lessonfour.server.auth.exception.DuplicateEmailException;
import depromeet.lessonfour.server.auth.exception.DuplicateNicknameException;
import depromeet.lessonfour.server.auth.persist.jpa.UserRepository;
import depromeet.lessonfour.server.auth.persist.jpa.entity.User;
import depromeet.lessonfour.server.common.annotation.UseCase;
import lombok.RequiredArgsConstructor;

@UseCase
@Transactional
@RequiredArgsConstructor
public class RegisterUseCase {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public void register(RegisterRequestDto dto) {
    if (userRepository.existsByEmail(dto.email())) {
      throw new DuplicateEmailException(dto.email());
    }

    if (userRepository.existsByNickname(dto.nickname())) {
      throw new DuplicateNicknameException(dto.nickname());
    }

    String encodedPassword = passwordEncoder.encode(dto.password());
    User user = User.register(dto.email(), dto.nickname(), encodedPassword);
    userRepository.save(user);
  }
}
