package depromeet.lessonfour.server.auth.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.auth.api.dto.request.RegisterRequestDto;
import depromeet.lessonfour.server.auth.exception.DuplicateEmailException;
import depromeet.lessonfour.server.auth.exception.DuplicateNicknameException;
import depromeet.lessonfour.server.auth.persist.jpa.UserRepository;
import depromeet.lessonfour.server.auth.persist.jpa.entity.User;
import depromeet.lessonfour.server.auth.service.validator.UserRegisterValidator;
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

  public void register(RegisterRequestDto dto) {

    String email = dto.email().toLowerCase();
    String nickname = dto.nickname().trim();

    userRegisterValidator.duplicateEmailCheck(email);
    userRegisterValidator.duplicateNicknameCheck(nickname);
    String encodedPassword = passwordEncoder.encode(dto.password());

    User user = User.register(dto.email(), dto.nickname(), encodedPassword);
    try {
      userRepository.save(user);
    } catch (DataIntegrityViolationException e) {
      String errorMessage = e.getMostSpecificCause().getMessage();
      log.warn("데이터 무결성 위반: {}", errorMessage, e);

      if (errorMessage.toLowerCase().contains("email") || errorMessage.contains("uk_user_email")) {
        throw new DuplicateEmailException(email);
      } else if (errorMessage.toLowerCase().contains("nickname")
          || errorMessage.contains("uk_user_nickname")) {
        throw new DuplicateNicknameException(nickname);
      }
      // 어떤 필드가 중복인지 확실하지 않은 경우 기본적으로 이메일 중복으로 처리
      throw new DuplicateEmailException(email);
    }
  }
}
