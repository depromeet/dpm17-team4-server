package depromeet.lessonfour.server.common.api.handler;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import depromeet.lessonfour.server.common.api.code.BaseErrorCode;
import depromeet.lessonfour.server.common.api.code.ErrorCode;
import depromeet.lessonfour.server.common.api.dto.ErrorResponse;
import depromeet.lessonfour.server.common.exception.ServerException;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ServerException.class)
  public ResponseEntity<ErrorResponse> handleServerException(ServerException e) {
    BaseErrorCode errorCode = e.getBaseErrorCode();
    return ResponseEntity.status(errorCode.getHttpStatus()).body(ErrorResponse.of(errorCode));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationExceptions(
      MethodArgumentNotValidException e) {
    // 가장 먼저 발생한 필드 에러 메시지(없으면 기본 메시지)
    String detail =
        e.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(err -> String.format("%s: %s", err.getField(), err.getDefaultMessage()))
            .orElse("요청 값 검증에 실패했습니다.");
    return buildErrorResponse(ErrorCode.INVALID_FIELD_ERROR, detail);
  }

  @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
  public ResponseEntity<ErrorResponse> handleAuthExceptions(AuthenticationException e) {
    // 인증 실패 (자격 증명 오류 등)
    return buildErrorResponse(ErrorCode.UNAUTHORIZED, e.getMessage());
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolationException(
      ConstraintViolationException e) {
    return buildErrorResponse(ErrorCode.INVALID_FIELD_ERROR, e.getMessage());
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
      MissingServletRequestParameterException e) {
    return buildErrorResponse(ErrorCode.MISSING_PARAMETER, e.getParameterName());
  }

  @ExceptionHandler(MissingRequestHeaderException.class)
  public ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(
      MissingRequestHeaderException e) {
    return buildErrorResponse(ErrorCode.MISSING_HEADER, e.getHeaderName());
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatchException(
      MethodArgumentTypeMismatchException e) {
    String detail =
        e.getRequiredType() != null
            ? String.format(
                "'%s'은(는) %s 타입이어야 합니다.", e.getName(), e.getRequiredType().getSimpleName())
            : "타입 변환 오류입니다.";
    return buildErrorResponse(ErrorCode.TYPE_MISMATCH, detail);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException e) {
    return buildErrorResponse(ErrorCode.INVALID_REQUEST_BODY, e.getMessage());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
      DataIntegrityViolationException e) {
    return buildErrorResponse(ErrorCode.DATA_INTEGRITY_VIOLATION, e.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
    return buildErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupported(
      HttpMediaTypeNotSupportedException e) {
    String detail =
        (e.getContentType() != null)
            ? "요청 Content-Type: " + e.getContentType()
            : "요청 Content-Type 누락";
    return buildErrorResponse(ErrorCode.UNSUPPORTED_MEDIA_TYPE, detail);
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException e) {
    String detail = String.format("잘못된 요청 경로: %s %s", e.getHttpMethod(), e.getRequestURL());
    return buildErrorResponse(ErrorCode.PATH_NOT_FOUND, detail);
  }

  private ResponseEntity<ErrorResponse> buildErrorResponse(BaseErrorCode errorCode, Object detail) {
    return ResponseEntity.status(errorCode.getHttpStatus())
        .body(ErrorResponse.of(errorCode, detail));
  }
}
