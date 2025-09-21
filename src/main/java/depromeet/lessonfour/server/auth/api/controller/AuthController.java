package depromeet.lessonfour.server.auth.api.controller;

import static depromeet.lessonfour.server.auth.infra.security.jwt.JwtConstants.REFRESH_TOKEN_COOKIE_NAME;

import java.net.URI;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import depromeet.lessonfour.server.auth.app.dto.response.AuthTokenDto;
import depromeet.lessonfour.server.auth.app.service.RefreshTokenUseCase;
import depromeet.lessonfour.server.user.app.dto.request.LoginRequestDto;
import depromeet.lessonfour.server.user.app.dto.request.RegisterRequestDto;
import depromeet.lessonfour.server.user.app.dto.response.AccessTokenResponseDto;
import depromeet.lessonfour.server.user.app.service.SignupUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "인증", description = "로컬 회원가입 및 토큰 갱신 API 문서입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final SignupUseCase registerUseCase;
  private final RefreshTokenUseCase refreshTokenUseCase;

  @Operation(
      summary = "로컬 회원가입",
      description = "이메일과 비밀번호로 회원가입을 진행합니다. 성공 시 201 Created와 함께 생성된 유저 정보를 반환합니다.")
  @PostMapping(
      path = "/signup",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> signup(@Valid @RequestBody RegisterRequestDto dto) {
    // TODO: RESTful하게 users 도메인으로 옮기는 것은 어떨까? e.g. POST users
    var user = registerUseCase.signup(dto);
    return ResponseEntity.created(URI.create("/api/v1/users/" + user.id())).body(user);
  }

  @Operation(
      summary = "로컬 로그인",
      description = "이메일과 비밀번호로 로그인을 진행합니다. 성공 시 200 OK와 함께 access token을 반환합니다.")
  @PostMapping(
      path = "/login",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AccessTokenResponseDto> login(@Valid @RequestBody LoginRequestDto dto) {
    /** swagger에서만 사용 실제 로그인은 spring security filter에서 처리 이 메서드가 호출되지 않음 */
    return ResponseEntity.ok().body(new AccessTokenResponseDto(null));
  }

  @Operation(
      summary = "토큰 갱신",
      description = "만료된 access token을 갱신합니다.",
      security = {@SecurityRequirement(name = "JWT")})
  @PostMapping("/refresh")
  public ResponseEntity<?> refresh(
      @CookieValue(value = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken) {

    if (refreshToken == null || refreshToken.isBlank()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token not found");
    }

    AuthTokenDto result = refreshTokenUseCase.refresh(refreshToken);

    // Refresh Token Rotation: 새로운 refresh token을 쿠키로 업데이트
    ResponseCookie refreshTokenCookie =
        ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, result.refreshToken())
            .httpOnly(true)
            .sameSite("Strict")
            .maxAge(7 * 24 * 60 * 60) // 7일
            .path("/")
            .build();
    // .secure(true) // HTTPS에서만 전송
    System.out.println("refresh_token: " + result.refreshToken());
    return ResponseEntity.ok()
        .header("Set-Cookie", refreshTokenCookie.toString())
        .cacheControl(CacheControl.noStore().mustRevalidate())
        .body(new AccessTokenResponseDto(result.accessToken()));
  }
}
