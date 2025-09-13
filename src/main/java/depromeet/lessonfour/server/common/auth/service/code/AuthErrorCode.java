package depromeet.lessonfour.server.common.auth.service.code;

import org.springframework.http.HttpStatus;

import depromeet.lessonfour.server.common.exception.base.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {
  /*
  409 Conflict
  */
  DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
  DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용중인 이름이에요."),
  ;

  private final HttpStatus httpStatus;
  private final String message;
}
