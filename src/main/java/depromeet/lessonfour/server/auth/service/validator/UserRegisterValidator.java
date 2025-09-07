package depromeet.lessonfour.server.auth.service.validator;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.auth.exception.DuplicateEmailException;
import depromeet.lessonfour.server.auth.exception.DuplicateNicknameException;
import depromeet.lessonfour.server.auth.persist.jpa.UserRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserRegisterValidator {

  private final UserRepository userRepository;

  public void duplicateEmailCheck(String email) {
    if (userRepository.existsByEmail(email)) {
      throw new DuplicateEmailException(email);
    }
  }

  public void duplicateNicknameCheck(String nickname) {
    if (userRepository.existsByNickname(nickname)) {
      throw new DuplicateNicknameException(nickname);
    }
  }
}
