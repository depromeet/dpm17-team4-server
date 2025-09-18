package depromeet.lessonfour.server.common.exception.code;

import org.springframework.http.HttpStatus;

public interface BaseSuccessCode {
  HttpStatus getHttpStatus();

  String getMessage();
}
