package depromeet.lessonfour.server.auth.exception;

// TODO : GlobalExceptionHandler 추가 후 통합
public class DuplicateEmailException extends RuntimeException {

  public DuplicateEmailException(String email) {
    super("이미 사용 중인 이메일입니다.");
  }
}
