package depromeet.lessonfour.server.auth;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import depromeet.lessonfour.server.user.User;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final KakaoAuthService kakaoAuthService;

  @Value("${kakao.client-id}")
  private String kakaoClientId;

  @Value("${kakao.redirect-uri}")
  private String kakaoRedirectUri;

  public AuthController(KakaoAuthService kakaoAuthService) {
    this.kakaoAuthService = kakaoAuthService;
  }

  @GetMapping("/kakao/login")
  public ResponseEntity<Void> kakaoLogin() {
    MultiValueMap<String, String> authParams =
        new LinkedMultiValueMap<>() {
          {
            add("client_id", kakaoClientId);
            add("redirect_uri", kakaoRedirectUri);
            add("response_type", "code");
            add("scope", "openid profile_nickname profile_image account_email");
          }
        };

    String authUrl =
        UriComponentsBuilder.fromUriString("https://kauth.kakao.com/oauth/authorize")
            .queryParams(authParams)
            .build()
            .toUriString();

    return ResponseEntity.status(302).header("Location", authUrl).build();
  }

  @GetMapping("/kakao/callback")
  public ResponseEntity<Void> kakaoCallback(
      @RequestParam(required = false) String code, @RequestParam(required = false) String error) {

    if (error != null) {
      System.out.println("OAuth error: " + error);
      String errorUrl =
          UriComponentsBuilder.fromUriString("http://localhost:3000")
              .queryParam("error", "OAuth error: " + error)
              .encode(StandardCharsets.UTF_8)
              .build()
              .toUriString();
      return ResponseEntity.status(302).header("Location", errorUrl).build();
    }

    if (code != null) {
      System.out.println("Received code: " + code);
      try {
        Map<String, Object> tokenResponse = kakaoAuthService.getToken(code);
        User user = kakaoAuthService.processOidcToken(tokenResponse.get("id_token").toString());
        String accessToken = "access_token_" + UUID.randomUUID().toString();
        String refreshToken = "refresh_token_" + UUID.randomUUID().toString();
        ResponseCookie refreshTokenCookie =
            ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .sameSite("Strict")
                .maxAge(7 * 24 * 60 * 60) // 7일
                .path("/")
                .build();
        // .secure(true) // HTTPS에서만 전송
        System.out.println("Is new user? " + user.isNew());
        String successUrl =
            UriComponentsBuilder.fromUriString("http://localhost:3000")
                .queryParam("access_token", accessToken)
                .queryParam("user_id", user.getId())
                .queryParam("username", user.getUsername())
                .queryParam("profile_image", user.getProfileImage())
                .queryParam("is_new_user", user.isNew())
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUriString();

        System.out.println(
            "User authenticated: " + user.getUsername() + " (" + user.getEmail() + ")");

        return ResponseEntity.status(302)
            .header("Location", successUrl)
            .header("Set-Cookie", refreshTokenCookie.toString())
            .build();

      } catch (Exception e) {
        System.out.println("Failed to get token: " + e.getMessage());
        String errorUrl =
            UriComponentsBuilder.fromUriString("http://localhost:3000")
                .queryParam("error", "Authentication failed: " + e.getMessage())
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUriString();
        return ResponseEntity.status(302).header("Location", errorUrl).build();
      }
    }

    System.out.println("No code or error received");
    String errorUrl =
        UriComponentsBuilder.fromUriString("http://localhost:3000")
            .queryParam("error", "No code or error received")
            .encode(StandardCharsets.UTF_8)
            .build()
            .toUriString();
    return ResponseEntity.status(302).header("Location", errorUrl).build();
  }
}
