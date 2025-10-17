package depromeet.lessonfour.server.user.app.service;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.auth.app.validator.UserRegisterValidator;
import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.user.app.dto.request.RegisterRequestDto;
import depromeet.lessonfour.server.user.app.dto.response.UserResponseDto;
import depromeet.lessonfour.server.user.domain.entity.User;
import depromeet.lessonfour.server.user.domain.repository.UserRepository;
import depromeet.lessonfour.server.user.domain.vo.Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UseCase
@Transactional
@RequiredArgsConstructor
public class SignupUseCase {

  private final UserRegisterValidator userRegisterValidator;
  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;

  public UserResponseDto signup(RegisterRequestDto dto) {

    String email = dto.email().toLowerCase(Locale.ROOT);
    String nickname = dto.nickname().trim();

    userRegisterValidator.duplicateEmailCheck(email);
    String encodedPassword = passwordEncoder.encode(dto.password());

    User user = User.register(email, nickname, encodedPassword, null, Provider.local());
    userRepository.save(user); // TODO : concurrency issue 용 uk 정의

    return UserResponseDto.of(user);
  }
}
