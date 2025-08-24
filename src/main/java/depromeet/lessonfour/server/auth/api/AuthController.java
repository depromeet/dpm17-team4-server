package depromeet.lessonfour.server.auth.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import depromeet.lessonfour.server.auth.api.dto.request.ReIssueRequestDto;
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
      @Valid @RequestBody ReIssueRequestDto dto, HttpServletResponse response) {
    ReIssueResult result = reIssueTokenUseCase.reIssue(dto);

    servletUtils.addCookie(
        response,
        HttpServletUtils.REFRESH_TOKEN_COOKIE_NAME,
        result.refreshToken(),
        HttpServletUtils.REFRESH_TOKEN_EXPIRATION);

    return ResponseEntity.ok(new AccessTokenResponseDto(result.accessToken()));
  }
}
