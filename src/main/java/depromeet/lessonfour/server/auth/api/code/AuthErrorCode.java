package depromeet.lessonfour.server.auth.api.code;

import org.springframework.http.HttpStatus;

import depromeet.lessonfour.server.common.api.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {
  /*
  400 Bad Request
   */
  ID_TOKEN_REQUIRED(HttpStatus.BAD_REQUEST, "ID 토큰이 필요해요."),
  EMAIL_REQUIRED_FOR_OIDC(HttpStatus.BAD_REQUEST, "OIDC 인증에 이메일이 필요합니다."),

  /*
  401 Unauthorized
   */
  INVALID_OIDC_TOKEN(HttpStatus.UNAUTHORIZED, "OIDC 토큰 검증에 실패했어요."),

  /*
  404 Not Found
   */
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
  OAUTH_PROVIDER_NOT_FOUND(HttpStatus.NOT_FOUND, "OAuth 공급자를 찾을 수 없습니다."),

  /*
  409 Conflict
  */
  DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
  DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용중인 이름이에요."),

  /*
   * 500 Internal Server Error
   * - 외부 통신 문제라기보다, 로컬에서 JWK 파싱/키 생성 단계에서 실패한 경우
   */
  JWKS_PUBLIC_KEY_BUILD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "JWK 공개키 생성에 실패했어요."),

  /*
   * 502 Bad Gateway
   * - OAuth 공급자와의 통신, 토큰/키 조회 같은 upstream 연동 실패
   */
  OAUTH_TOKEN_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "OAuth 토큰 발급 요청이 실패했어요."),
  JWKS_RESPONSE_INVALID(HttpStatus.BAD_GATEWAY, "JWKS 응답이 유효하지 않아요."), // 'keys' 배열 없음, 비정상 포맷 등
  ;

  private final HttpStatus httpStatus;
  private final String message;
}
