package depromeet.lessonfour.server.auth.app.validator;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.auth.api.code.AuthErrorCode;
import depromeet.lessonfour.server.common.exception.ServerException;
import depromeet.lessonfour.server.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserRegisterValidator {

  private final UserRepository userRepository;

  public void duplicateEmailCheck(String email) {
    if (userRepository.existsActiveByEmail(email)) {
      throw new ServerException(AuthErrorCode.DUPLICATE_EMAIL);
    }
  }
}
