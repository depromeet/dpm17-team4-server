package depromeet.lessonfour.server.auth.exception;

// TODO : GlobalExceptionHandler 추가 후 통합
public class DuplicateNicknameException extends RuntimeException {

  public DuplicateNicknameException(String nickname) {
    super("Nickname already exists: " + nickname);
  }
}
