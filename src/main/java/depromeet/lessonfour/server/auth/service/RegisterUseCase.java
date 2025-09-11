package depromeet.lessonfour.server.auth.service;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.auth.api.dto.request.RegisterRequestDto;
import depromeet.lessonfour.server.auth.api.dto.response.UserResponseDto;
import depromeet.lessonfour.server.auth.persist.jpa.UserRepository;
import depromeet.lessonfour.server.auth.persist.jpa.entity.User;
import depromeet.lessonfour.server.auth.service.validator.UserRegisterValidator;
import depromeet.lessonfour.server.auth.value.Provider;
import depromeet.lessonfour.server.common.annotation.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UseCase
@Transactional
@RequiredArgsConstructor
public class RegisterUseCase {

  private final UserRegisterValidator userRegisterValidator;
  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;

  public UserResponseDto register(RegisterRequestDto dto) {

    String email = dto.email().toLowerCase(Locale.ROOT);
    String nickname = dto.nickname().trim();

    userRegisterValidator.duplicateEmailCheck(email);
    userRegisterValidator.duplicateNicknameCheck(nickname);
    String encodedPassword = passwordEncoder.encode(dto.password());

    User user = User.register(email, nickname, encodedPassword, null, Provider.local());
    userRepository.save(user); // TODO : concurrency issue 용 uk 정의
    return UserResponseDto.of(user);
  }
}
