package depromeet.lessonfour.server.common.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import depromeet.lessonfour.server.auth.exception.DuplicateEmailException;
import depromeet.lessonfour.server.auth.exception.DuplicateNicknameException;

// TODO : GlobalExceptionHandler 추가 후 통합
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    Map<String, String> response = new HashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(error -> response.put("message", error.getDefaultMessage()));
    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(DuplicateEmailException.class)
  public ResponseEntity<Map<String, String>> handleDuplicateEmailException(
      DuplicateEmailException ex) {
    Map<String, String> response = new HashMap<>();
    response.put("message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }

  @ExceptionHandler(DuplicateNicknameException.class)
  public ResponseEntity<Map<String, String>> handleDuplicateNicknameException(
      DuplicateNicknameException ex) {
    Map<String, String> response = new HashMap<>();
    response.put("message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }
}
