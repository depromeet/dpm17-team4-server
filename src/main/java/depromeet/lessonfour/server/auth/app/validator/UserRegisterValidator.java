package depromeet.lessonfour.server.auth.app.validator;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.auth.api.code.AuthErrorCode;
import depromeet.lessonfour.server.common.exception.ServerException;
import depromeet.lessonfour.server.user.infra.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserRegisterValidator {

  private final UserRepository userRepository;

  public void duplicateEmailCheck(String email) {
    if (userRepository.existsByEmail(email)) {
      throw new ServerException(AuthErrorCode.DUPLICATE_EMAIL);
    }
  }

  /*
  public void duplicateNicknameCheck(String nickname) {
    if (userRepository.existsByNickname(nickname)) {
      throw new ServerException(AuthErrorCode.DUPLICATE_NICKNAME);
    }
  }
  */
}
