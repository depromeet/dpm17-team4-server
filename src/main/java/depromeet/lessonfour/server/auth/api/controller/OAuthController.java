package depromeet.lessonfour.server.auth.api.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import depromeet.lessonfour.server.auth.api.code.AuthErrorCode;
import depromeet.lessonfour.server.auth.api.dto.AuthCodeRequestDto;
import depromeet.lessonfour.server.auth.api.util.RefreshTokenCookieGenerator;
import depromeet.lessonfour.server.auth.app.dto.response.AuthResponseDto;
import depromeet.lessonfour.server.auth.app.service.AppleUserCache;
import depromeet.lessonfour.server.auth.app.service.AuthCodeFlowUseCase;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "소셜 로그인 인증", description = "소셜 로그인 인증에 대한 API 문서입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class OAuthController {

  private final AuthCodeFlowUseCase authCodeFlowUseCase;
  private final OAuthCallbackUseCase oAuthCallbackUseCase;
  private final OidcStateCodec oidcStateCodec;
  private final AppleUserCache appleUserCache;

  @Operation(summary = "소셜 로그인", description = "소셜 로그인을 진행합니다.")
  @PostMapping("/{provider}/login")
  public ResponseEntity<Void> oauthLogin(
      @PathVariable("provider") String provider,
      @RequestParam(required = false) String redirectUri,
      @RequestParam(required = false) String responseType) {

    String encodedState =
        oidcStateCodec.encode(
            redirectUri != null ? redirectUri : "", responseType != null ? responseType : "");

    String redirectUrl =
        UriComponentsBuilder.fromPath(String.format("/oauth2/authorization/%s", provider))
            .queryParam("state", encodedState)
            .build()
            .toUriString();

    return ResponseEntity.status(302).header("Location", redirectUrl).build();
  }

  @Operation(summary = "인증 토큰 발급", description = "인가 코드를 받아서 토큰을 JSON 형태로 반환합니다.")
  @PostMapping("/{provider}/token")
  public ResponseEntity<AuthResponseDto> getToken(
      @PathVariable("provider") String provider, @Valid @RequestBody AuthCodeRequestDto request) {
    try {
      // JSON 응답에서는 access token도 포함
      AuthResponseDto authResult = authCodeFlowUseCase.login(request.code(), provider, true);

      return ResponseEntity.ok()
          .cacheControl(CacheControl.noStore().mustRevalidate())
          .body(authResult);
    } catch (Exception e) {
      log.error("Authentication failed: {}", e.getMessage());
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Authentication failed: " + e.getMessage());
    }
  }

  @Hidden
  @Operation(summary = "소셜 로그인 인증 콜백 (GET)", description = "소셜 로그인 인증 후 콜백을 처리합니다.")
  @GetMapping("/{provider}/callback")
  public void callback(
      @PathVariable("provider") String provider,
      @Parameter(description = "인가코드") @RequestParam String code,
      @Parameter(description = "state (redirectUri|responseType 인코딩)") @RequestParam String state,
      @Parameter(description = "에러 발생시") @RequestParam(required = false) String error,
      HttpServletResponse response)
      throws IOException {
    handleCallback(provider, code, state, error, response);
  }

  @Hidden
  @Operation(
      summary = "소셜 로그인 인증 콜백 (POST)",
      description = "Apple의 form_post response_mode를 위한 POST 콜백 처리")
  @PostMapping("/{provider}/callback")
  public void callbackPost(
      @PathVariable("provider") String provider,
      @Parameter(description = "인가코드") @RequestParam String code,
      @Parameter(description = "state (redirectUri|responseType 인코딩)") @RequestParam String state,
      @Parameter(description = "에러 발생시") @RequestParam(required = false) String error,
      @Parameter(description = "Apple user info (첫 로그인시만)") @RequestParam(required = false)
          String user,
      HttpServletResponse response)
      throws IOException {
    // user 정보는 로깅만 하고 실제로는 id_token에서 파싱
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

    AuthResponseDto authResponseDto = oAuthCallbackUseCase.login(provider, code);
    String domain = UriUtils.extractDomain(stateData.redirectUri());

    ResponseCookie cookie =
        (domain != null && !domain.isBlank() && !domain.equals("localhost"))
            ? RefreshTokenCookieGenerator.generate(authResponseDto.refreshToken(), domain)
            : RefreshTokenCookieGenerator.generate(authResponseDto.refreshToken());

    response.addHeader("Set-Cookie", cookie.toString());

    response.sendRedirect(
        UriComponentsBuilder.fromUriString(stateData.redirectUri())
            .queryParam("id", authResponseDto.id())
            .queryParam("email", authResponseDto.email())
            .queryParam("nickname", authResponseDto.nickname())
            .queryParam("providerType", authResponseDto.providerType())
            .queryParam("profileImage", authResponseDto.profileImage())
            .queryParam("birthYear", authResponseDto.birthYear())
            .queryParam("gender", authResponseDto.gender())
            .queryParam("isNew", authResponseDto.isNew())
            .encode(StandardCharsets.UTF_8)
            .build()
            .toUriString());
  }
}
