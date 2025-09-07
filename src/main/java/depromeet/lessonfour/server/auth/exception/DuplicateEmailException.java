package depromeet.lessonfour.server.auth.exception;

// TODO : GlobalExceptionHandler 추가 후 통합
public class DuplicateEmailException extends RuntimeException {

  public DuplicateEmailException(String email) {
    super("Email already exists: " + email);
  }
}
