package depromeet.lessonfour.server.auth.api.controller;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import depromeet.lessonfour.server.auth.api.util.RefreshTokenCookieGenerator;
import depromeet.lessonfour.server.auth.app.service.KakaoAuthService;
import depromeet.lessonfour.server.user.app.dto.response.AuthResponseDto;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "카카오 인증", description = "카카오 인증에 대한 API 문서입니다.")
@RestController
@RequestMapping("/api/v1/auth/kakao")
public class KakaoAuthController {

  private final KakaoAuthService kakaoAuthService;

  @Value("${kakao.client-id}")
  private String kakaoClientId;

  @Value("${kakao.redirect-url}")
  private String kakaoRedirectUrl;

  @Value("${frontend.url}")
  private String frontendUrl;

  @Value("${kakao.auth-url}")
  private String kakaoAuthUrl;

  public KakaoAuthController(KakaoAuthService kakaoAuthService) {
    this.kakaoAuthService = kakaoAuthService;
  }

  @Operation(summary = "카카오 로그인", description = "카카오를 통해 로그인을 진행합니다.")
  @PostMapping("/login")
  public ResponseEntity<Void> kakaoLogin(@RequestParam(required = false) String redirectUri) {
    String authUrl =
        kakaoAuthService.getRequestUrl(redirectUri != null ? redirectUri : frontendUrl);
    return ResponseEntity.status(302).header("Location", authUrl).build();
  }

  @Hidden
  @GetMapping("/callback")
  public ResponseEntity<Void> kakaoLegacyCallback(
      @RequestParam(required = false) String code,
      @RequestParam(required = false) String error,
      @RequestParam(required = false) String state) {
    log.info(
        "[Kakao Callback] Starting callback processing - code present: {}, error present: {}, state: {}",
        code != null,
        error != null,
        state);

    String clientRedirectUri = state != null ? state : frontendUrl;
    log.debug("[Kakao Callback] Client redirect URI determined: {}", clientRedirectUri);

    if (error != null) {
      log.error("[Kakao Callback] OAuth error received from Kakao: {}", error);
      String errorUrl =
          UriComponentsBuilder.fromUriString(clientRedirectUri)
              .queryParam("error", "OAuth error: " + error)
              .encode(StandardCharsets.UTF_8)
              .build()
              .toUriString();
      log.info("[Kakao Callback] Redirecting to error URL: {}", errorUrl);
      return ResponseEntity.status(302).header("Location", errorUrl).build();
    }

    if (code != null) {
      log.info(
          "[Kakao Callback] Processing authorization code: {}",
          code.substring(0, Math.min(code.length(), 10)) + "...");
      try {
        log.debug("[Kakao Callback] Calling KakaoAuthService.login with code");
        AuthResponseDto authResult = kakaoAuthService.login(code);
        log.info(
            "[Kakao Callback] Authentication successful - userId: {}, isNew: {}, provider: {}",
            authResult.id(),
            authResult.isNew(),
            authResult.provider().getType());

        log.debug("[Kakao Callback] Generating refresh token cookie");
        ResponseCookie refreshTokenCookie =
            RefreshTokenCookieGenerator.generate(authResult.refreshToken());
        log.debug("[Kakao Callback] Refresh token cookie generated successfully");

        String successUrl =
            UriComponentsBuilder.fromUriString(clientRedirectUri)
                .queryParam("id", authResult.id())
                .queryParam("nickname", authResult.nickname())
                .queryParam("profileImage", authResult.profileImage())
                .queryParam("isNew", authResult.isNew())
                .queryParam("providerType", authResult.provider().getType())
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUriString();
        log.info("[Kakao Callback] Redirecting to success URL: {}", successUrl);

        return ResponseEntity.status(302)
            .header("Location", successUrl)
            .header("Set-Cookie", refreshTokenCookie.toString())
            .cacheControl(CacheControl.noStore().mustRevalidate())
            .build();

      } catch (Exception e) {
        log.error("[Kakao Callback] Authentication failed with exception: {}", e.getMessage(), e);
        String errorUrl =
            UriComponentsBuilder.fromUriString(clientRedirectUri)
                .queryParam("error", "Authentication failed: " + e.getMessage())
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUriString();
        log.info("[Kakao Callback] Redirecting to error URL due to exception: {}", errorUrl);
        return ResponseEntity.status(302).header("Location", errorUrl).build();
      }
    }

    log.warn("[Kakao Callback] No authorization code or error received from Kakao");
    String errorUrl =
        UriComponentsBuilder.fromUriString(clientRedirectUri)
            .queryParam("error", "No code or error received")
            .encode(StandardCharsets.UTF_8)
            .build()
            .toUriString();
    log.info("[Kakao Callback] Redirecting to error URL - no code received: {}", errorUrl);
    return ResponseEntity.status(302).header("Location", errorUrl).build();
  }
}
