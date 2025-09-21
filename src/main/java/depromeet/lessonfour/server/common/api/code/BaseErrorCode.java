package depromeet.lessonfour.server.common.api.code;

import org.springframework.http.HttpStatus;

public interface BaseErrorCode {
  HttpStatus getHttpStatus();

  String getMessage();
}
