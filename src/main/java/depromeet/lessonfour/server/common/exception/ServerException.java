package depromeet.lessonfour.server.common.exception;

import depromeet.lessonfour.server.common.exception.code.BaseErrorCode;
import lombok.Getter;

@Getter
public class ServerException extends RuntimeException {
  private final BaseErrorCode baseErrorCode;

  public ServerException(BaseErrorCode baseErrorCode) {
    super(baseErrorCode.getMessage());
    this.baseErrorCode = baseErrorCode;
  }
}
