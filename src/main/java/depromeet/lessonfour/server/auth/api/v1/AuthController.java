package depromeet.lessonfour.server.auth.api.v1;

import static depromeet.lessonfour.server.auth.security.jwt.JwtConstants.REFRESH_TOKEN_COOKIE_NAME;

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

import depromeet.lessonfour.server.auth.api.dto.request.RegisterRequestDto;
import depromeet.lessonfour.server.auth.api.dto.response.AccessTokenResponseDto;
import depromeet.lessonfour.server.auth.service.ReIssueTokenUseCase;
import depromeet.lessonfour.server.auth.service.RegisterUseCase;
import depromeet.lessonfour.server.auth.service.dto.ReIssueResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final RegisterUseCase registerUseCase;
  private final ReIssueTokenUseCase reIssueTokenUseCase;

  @PostMapping("/signup")
  public ResponseEntity<?> signup(@Valid @RequestBody RegisterRequestDto dto) {
    var user = registerUseCase.register(dto);
    return ResponseEntity.created(URI.create("/api/v1/users/" + user.id())).body(user);
  }

  @PostMapping("/refresh")
  public ResponseEntity<?> refresh(
      @CookieValue(value = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken) {

    if (refreshToken == null || refreshToken.isBlank()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token not found");
    }

    ReIssueResult result = reIssueTokenUseCase.reIssue(refreshToken);

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
