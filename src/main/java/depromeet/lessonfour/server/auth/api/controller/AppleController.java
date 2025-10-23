package depromeet.lessonfour.server.auth.api.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import depromeet.lessonfour.server.auth.api.code.AuthErrorCode;
import depromeet.lessonfour.server.auth.api.util.RefreshTokenCookieGenerator;
import depromeet.lessonfour.server.auth.app.service.AppleUserCache;
import depromeet.lessonfour.server.auth.app.service.OAuthCallbackUseCase;
import depromeet.lessonfour.server.auth.domain.vo.StateData;
import depromeet.lessonfour.server.auth.infra.security.oauth.OidcStateCodec;
import depromeet.lessonfour.server.common.exception.ServerException;
import depromeet.lessonfour.server.common.util.UriUtils;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "소셜 로그인 인증 - Apple", description = "Apple 소셜 로그인 인증에 대한 API 문서입니다.")
@RestController
@RequiredArgsConstructor
public class AppleController {

  private final OidcStateCodec oidcStateCodec;
  private final OAuthCallbackUseCase oAuthCallbackUseCase;
  private final AppleUserCache appleUserCache;

  @Hidden
  @Operation(
      summary = "소셜 로그인 인증 콜백 (POST)",
      description = "Apple의 form_post response_mode를 위한 POST 콜백 처리")
  @PostMapping("/nika/login/callback")
  public void callbackPost(
      @Parameter(description = "인가코드") @RequestParam String code,
      @Parameter(description = "state (redirectUri|responseType 인코딩)") @RequestParam String state,
      @Parameter(description = "에러 발생시") @RequestParam(required = false) String error,
      @Parameter(description = "Apple user info (첫 로그인시만)") @RequestParam(required = false)
          String user,
      HttpServletResponse response)
      throws IOException {

    String provider = "apple";
    if (user != null) {
      log.info("Apple user info received: {}", user);
      appleUserCache.save(code, user);
    }
    handleCallback(provider, code, state, error, response);
  }

  private void handleCallback(
      String provider, String code, String state, String error, HttpServletResponse response)
      throws IOException {
    if (error != null) {
      throw new ServerException(AuthErrorCode.OAUTH_TOKEN_REQUEST_FAILED);
    }

    StateData stateData = oidcStateCodec.decode(state);

    // auth flow 진행
    if ("code".equalsIgnoreCase(stateData.responseType())) {
      response.sendRedirect(
          UriComponentsBuilder.fromUriString(stateData.redirectUri())
              .queryParam("code", code)
              .queryParam("providerType", provider.toUpperCase())
              .encode(StandardCharsets.UTF_8)
              .build()
              .toUriString());
      return;
    }

    String refreshToken = oAuthCallbackUseCase.login(provider, code);
    String domain = UriUtils.extractDomain(stateData.redirectUri());

    ResponseCookie cookie =
        (domain != null && !domain.isBlank())
            ? RefreshTokenCookieGenerator.generate(refreshToken, domain)
            : RefreshTokenCookieGenerator.generate(refreshToken);

    response.addHeader("Set-Cookie", cookie.toString());

    // same-domain 쿠키도 추가
    if (domain != null && !domain.isBlank()) {
      ResponseCookie localCookie = RefreshTokenCookieGenerator.generate(refreshToken);
      response.addHeader("Set-Cookie", localCookie.toString());
    }

    response.sendRedirect(
        UriComponentsBuilder.fromUriString(stateData.redirectUri())
            .encode(StandardCharsets.UTF_8)
            .build()
            .toUriString());
  }
}
