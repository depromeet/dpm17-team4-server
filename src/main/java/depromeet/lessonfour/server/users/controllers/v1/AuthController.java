package depromeet.lessonfour.server.users.controllers.v1;

import static depromeet.lessonfour.server.common.auth.security.jwt.JwtConstants.REFRESH_TOKEN_COOKIE_NAME;

import java.net.URI;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import depromeet.lessonfour.server.users.schemas.request.RegisterRequestDto;
import depromeet.lessonfour.server.users.schemas.response.AccessTokenResponseDto;
import depromeet.lessonfour.server.common.auth.service.RefreshTokenUseCase;
import depromeet.lessonfour.server.users.services.SignupUseCase;
import depromeet.lessonfour.server.common.auth.service.dto.AuthTokenDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final SignupUseCase registerUseCase;
  private final RefreshTokenUseCase refreshTokenUseCase;

  @PostMapping("/signup")
  public ResponseEntity<?> signup(@Valid @RequestBody RegisterRequestDto dto) {
    // TODO: RESTful하게 users 도메인으로 옮기는 것은 어떨까? e.g. POST users
    var user = registerUseCase.signup(dto);
    return ResponseEntity.created(URI.create("/api/v1/users/" + user.id())).body(user);
  }

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
