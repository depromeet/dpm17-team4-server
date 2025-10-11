package depromeet.lessonfour.server.auth.api.controller;

import java.net.URI;
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
    String clientRedirectUri = state != null ? state : frontendUrl;
    if (error != null) {
      String errorUrl =
          UriComponentsBuilder.fromUriString(clientRedirectUri)
              .queryParam("error", "OAuth error: " + error)
              .encode(StandardCharsets.UTF_8)
              .build()
              .toUriString();
      return ResponseEntity.status(302).header("Location", errorUrl).build();
    }

    if (code != null) {
      try {
        AuthResponseDto authResult = kakaoAuthService.login(code);

        // redirectUrl의 호스트를 도메인으로 사용
        String domain = null;
        try {
          URI redirectUri = URI.create(clientRedirectUri);
          domain = redirectUri.getHost();
        } catch (Exception e) {
          // URI 파싱 실패 시 도메인 없이 진행
        }

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

        ResponseEntity.BodyBuilder responseBuilder =
            ResponseEntity.status(302)
                .header("Location", successUrl)
                .cacheControl(CacheControl.noStore().mustRevalidate());

        // 도메인을 성공적으로 가져올 수 있으면 2개 쿠키 생성
        if (domain != null && !domain.isBlank()) {
          ResponseCookie refreshTokenCookieWithDomain =
              RefreshTokenCookieGenerator.generate(authResult.refreshToken(), domain);
          ResponseCookie refreshTokenCookieWithoutDomain =
              RefreshTokenCookieGenerator.generate(authResult.refreshToken());

          return responseBuilder
              .header("Set-Cookie", refreshTokenCookieWithDomain.toString())
              .header("Set-Cookie", refreshTokenCookieWithoutDomain.toString())
              .build();
        } else {
          // 도메인을 가져올 수 없으면 1개 쿠키만 생성
          ResponseCookie refreshTokenCookie =
              RefreshTokenCookieGenerator.generate(authResult.refreshToken());

          return responseBuilder.header("Set-Cookie", refreshTokenCookie.toString()).build();
        }

      } catch (Exception e) {
        String errorUrl =
            UriComponentsBuilder.fromUriString(clientRedirectUri)
                .queryParam("error", "Authentication failed: " + e.getMessage())
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUriString();
        return ResponseEntity.status(302).header("Location", errorUrl).build();
      }
    }

    String errorUrl =
        UriComponentsBuilder.fromUriString(clientRedirectUri)
            .queryParam("error", "No code or error received")
            .encode(StandardCharsets.UTF_8)
            .build()
            .toUriString();
    return ResponseEntity.status(302).header("Location", errorUrl).build();
  }
}
