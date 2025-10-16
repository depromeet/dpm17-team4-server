package depromeet.lessonfour.server.auth.api.controller;

import java.io.IOException;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
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

import depromeet.lessonfour.server.auth.api.dto.AuthCodeRequestDto;
import depromeet.lessonfour.server.auth.app.dto.response.AuthResponseDto;
import depromeet.lessonfour.server.auth.app.service.AuthCodeFlowUseCase;
import depromeet.lessonfour.server.auth.app.service.OAuthCallbackRedirectUseCase;
import depromeet.lessonfour.server.auth.infra.security.oauth.OidcStateCodec;
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
  private final OAuthCallbackRedirectUseCase oAuthCallbackRedirectUseCase;
  private final OidcStateCodec oidcStateCodec;

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
  @Operation(
      summary = "소셜 로그인 인증 콜백",
      description = "인가코드를 받습니다. responseType에 따라 인가코드만 전달하거나 Spring OAuth2로 위임합니다.")
  @GetMapping("/{provider}/callback")
  public void callback(
      @PathVariable("provider") String provider,
      @Parameter(description = "인가코드") @RequestParam String code,
      @Parameter(description = "state (redirectUri|responseType 인코딩)") @RequestParam String state,
      @Parameter(description = "에러 발생시") @RequestParam(required = false) String error,
      HttpServletResponse response)
      throws IOException {
    response.sendRedirect(oAuthCallbackRedirectUseCase.getPath(provider, code, state, error));
  }
}
