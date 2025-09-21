package depromeet.lessonfour.server.common.api.code;

import org.springframework.http.HttpStatus;

public interface BaseSuccessCode {
  HttpStatus getHttpStatus();

  String getMessage();
}
