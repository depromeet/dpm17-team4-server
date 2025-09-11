package depromeet.lessonfour.server.auth.api.v1;

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

import depromeet.lessonfour.server.auth.api.dto.response.AuthResponseDto;
import depromeet.lessonfour.server.auth.service.KakaoAuthService;

@RestController
@RequestMapping("/api/v1/auth/kakao")
public class KaKaoAuthController {

  private final KakaoAuthService kakaoAuthService;

  @Value("${kakao.client-id}")
  private String kakaoClientId;

  @Value("${kakao.redirect-url}")
  private String kakaoRedirectUrl;

  @Value("${frontend.url}")
  private String frontendUrl;

  @Value("${kakao.auth-url}")
  private String kakaoAuthUrl;

  public KaKaoAuthController(KakaoAuthService kakaoAuthService) {
    this.kakaoAuthService = kakaoAuthService;
  }

  @PostMapping("/login")
  public ResponseEntity<Void> kakaoLogin() {
    String authUrl = kakaoAuthService.getRequestUrl();
    return ResponseEntity.status(302).header("Location", authUrl).build();
  }

  @GetMapping("/callback")
  public ResponseEntity<Void> kakaoCallback(
      @RequestParam(required = false) String code, @RequestParam(required = false) String error) {

    if (error != null) {
      String errorUrl =
          UriComponentsBuilder.fromUriString(frontendUrl)
              .queryParam("error", "OAuth error: " + error)
              .encode(StandardCharsets.UTF_8)
              .build()
              .toUriString();
      return ResponseEntity.status(302).header("Location", errorUrl).build();
    }

    if (code != null) {
      try {
        AuthResponseDto authResult = kakaoAuthService.login(code);
        System.out.println("refresh_token: " + authResult.refreshToken());
        ResponseCookie refreshTokenCookie =
            ResponseCookie.from("refresh_token", authResult.refreshToken())
                .httpOnly(true)
                .sameSite("Strict")
                .maxAge(7 * 24 * 60 * 60) // 7일
                .path("/")
                .build();
        // .secure(true) // HTTPS에서만 전송

        String successUrl =
            UriComponentsBuilder.fromUriString(frontendUrl)
                .queryParam("id", authResult.id())
                .queryParam("nickname", authResult.nickname())
                .queryParam("profile_image", authResult.profileImage())
                .queryParam("is_new", authResult.isNew())
                .queryParam("provider_type", authResult.provider().getType())
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUriString();

        /*
        System.out.println(
            "User authenticated: " + user.getUsername() + " (" + user.getEmail() + ")");
        */

        return ResponseEntity.status(302)
            .header("Location", successUrl)
            .header("Set-Cookie", refreshTokenCookie.toString())
            .cacheControl(CacheControl.noStore().mustRevalidate())
            .build();

      } catch (Exception e) {
        String errorUrl =
            UriComponentsBuilder.fromUriString(frontendUrl)
                .queryParam("error", "Authentication failed: " + e.getMessage())
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUriString();
        return ResponseEntity.status(302).header("Location", errorUrl).build();
      }
    }

    String errorUrl =
        UriComponentsBuilder.fromUriString(frontendUrl)
            .queryParam("error", "No code or error received")
            .encode(StandardCharsets.UTF_8)
            .build()
            .toUriString();
    return ResponseEntity.status(302).header("Location", errorUrl).build();
  }
}
