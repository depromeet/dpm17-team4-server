package depromeet.lessonfour.server.common.exception.code;

import org.springframework.http.HttpStatus;

public interface BaseErrorCode {
  HttpStatus getHttpStatus();

  String getMessage();
}
