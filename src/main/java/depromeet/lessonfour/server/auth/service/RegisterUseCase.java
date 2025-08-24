package depromeet.lessonfour.server.auth.service;

import depromeet.lessonfour.server.auth.api.dto.request.RegisterRequestDto;
import depromeet.lessonfour.server.auth.persist.jpa.UserRepository;
import depromeet.lessonfour.server.auth.persist.jpa.entity.User;
import depromeet.lessonfour.server.auth.service.validator.UserRegisterValidator;
import depromeet.lessonfour.server.common.annotation.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@Transactional
@RequiredArgsConstructor
public class RegisterUseCase {

  private final UserRegisterValidator userRegisterValidator;
  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;

  public void register(RegisterRequestDto dto) {

    userRegisterValidator.duplicateEmailCheck(dto.email());
    userRegisterValidator.duplicateNicknameCheck(dto.nickname());

    String encodedPassword = passwordEncoder.encode(dto.password());
    User user = User.register(dto.email(), dto.nickname(), encodedPassword);
    userRepository.save(user);
  }
}
