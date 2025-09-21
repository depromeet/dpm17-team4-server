package depromeet.lessonfour.server.common.api.code;

import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ErrorCode implements BaseErrorCode {

  /*
  400 BAD REQUEST
   */
  INVALID_FIELD_ERROR(HttpStatus.BAD_REQUEST, "요청 필드 값이 유효하지 않습니다."),
  MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "필수 요청 파라미터가 누락되었습니다."),
  MISSING_HEADER(HttpStatus.BAD_REQUEST, "필수 요청 헤더가 누락되었습니다."),
  TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "요청 값 타입이 올바르지 않습니다."),
  INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, "요청 본문이 올바르지 않습니다."),
  DATA_INTEGRITY_VIOLATION(HttpStatus.BAD_REQUEST, "데이터 무결성 제약 조건을 위반했습니다."),
  BUSINESS_LOGIC_ERROR(HttpStatus.BAD_REQUEST, "비즈니스 로직 처리 중 오류가 발생했습니다."),
  INVALID_CURSOR_FORMAT(HttpStatus.BAD_REQUEST, "유효하지 않은 커서입니다."),
  INVALID_SORT_OPTION(HttpStatus.BAD_REQUEST, "유효하지 않은 정렬기준입니다."),

  /*
  401 UNAUTHORIZED
  */
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증에 실패했습니다."),

  /*
  404 NOT FOUND
  */
  DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "데이터가 존재하지 않습니다"),
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "유저가 존재하지 않습니다"),
  PATH_NOT_FOUND(HttpStatus.NOT_FOUND, "잘못된 요청 경로입니다."),

  /*
  409 CONFLICT
  */
  CONFLICT(HttpStatus.CONFLICT, "요청이 현재 리소스 상태와 충돌합니다."),

  /*
  415 UNSUPPORTED MEDIA TYPE
  */
  UNSUPPORTED_MEDIA_TYPE(
      HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 Content-Type 입니다. application/json을 사용하세요."),

  /*
  500 INTERNAL SERVER ERROR
  */
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부에서 오류가 발생했습니다.");

  private final HttpStatus httpStatus;
  private final String message;

  public HttpStatus getHttpStatus() {
    return httpStatus;
  }

  public String getMessage() {
    return message;
  }
}
