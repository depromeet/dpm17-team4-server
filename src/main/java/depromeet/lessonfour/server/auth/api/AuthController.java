package depromeet.lessonfour.server.auth.api;

import static depromeet.lessonfour.server.auth.config.jwt.JwtConstants.REFRESH_TOKEN_COOKIE_NAME;
import static depromeet.lessonfour.server.auth.config.jwt.JwtConstants.REFRESH_TOKEN_EXPIRATION;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
import depromeet.lessonfour.server.common.utils.HttpServletUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

  private final RegisterUseCase registerUseCase;
  private final ReIssueTokenUseCase reIssueTokenUseCase;
  private final HttpServletUtils servletUtils;

  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDto dto) {
    registerUseCase.register(dto);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/reissue")
  public ResponseEntity<?> reIssue(
      @CookieValue(value = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
      HttpServletResponse response) {

    if (refreshToken == null || refreshToken.isBlank()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token not found");
    }

    ReIssueResult result = reIssueTokenUseCase.reIssue(refreshToken);

    servletUtils.addCookie(
        response, REFRESH_TOKEN_COOKIE_NAME, result.refreshToken(), REFRESH_TOKEN_EXPIRATION);

    return ResponseEntity.ok()
        .cacheControl(CacheControl.noCache())
        .body(new AccessTokenResponseDto(result.accessToken()));
  }
}
