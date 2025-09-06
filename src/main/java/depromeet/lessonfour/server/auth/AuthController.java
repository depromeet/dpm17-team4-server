package depromeet.lessonfour.server.auth;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

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
  public ResponseEntity<String> kakaoCallback(
      @RequestParam(required = false) String code, @RequestParam(required = false) String error) {

    if (error != null) {
      System.out.println("OAuth error: " + error);
      return ResponseEntity.badRequest().body("OAuth error: " + error);
    }

    if (code != null) {
      System.out.println("Received code: " + code);
      try {
        Map<String, Object> tokenResponse = kakaoAuthService.getToken(code);
        kakaoAuthService.validateOidcToken(tokenResponse.get("id_token").toString());
        return ResponseEntity.ok("Token received: " + tokenResponse);
      } catch (Exception e) {
        System.out.println("Failed to get token: " + e.getMessage());
        return ResponseEntity.status(401).body("Authentication failed: " + e.getMessage());
      }
    }

    System.out.println("No code or error received");
    return ResponseEntity.badRequest().body("No code or error received");
  }
}
