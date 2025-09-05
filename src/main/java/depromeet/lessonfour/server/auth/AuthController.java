package depromeet.lessonfour.server.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  @Value("${kakao.client-id}")
  private String kakaoClientId;

  @Value("${kakao.redirect-uri}")
  private String kakaoRedirectUri;

  @GetMapping("/kakao/login")
  public ResponseEntity<Void> kakaoLogin() {
    MultiValueMap<String, String> authParams = new LinkedMultiValueMap<>();
    authParams.add("client_id", kakaoClientId);
    authParams.add("redirect_uri", kakaoRedirectUri);
    authParams.add("response_type", "code");
    authParams.add("scope", "openid profile_nickname profile_image account_email");
    
    String authUrl = UriComponentsBuilder
        .fromUriString("https://kauth.kakao.com/oauth/authorize")
        .queryParams(authParams)
        .build()
        .toUriString();
    
    return ResponseEntity.status(302)
        .header("Location", authUrl)
        .build();
  }
}
