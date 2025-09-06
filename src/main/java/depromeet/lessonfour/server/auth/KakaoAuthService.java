package depromeet.lessonfour.server.auth;

import java.security.Key;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import depromeet.lessonfour.server.user.User;
import depromeet.lessonfour.server.user.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Locator;

@Service
public class KakaoAuthService {

  private final RestTemplate restTemplate;
  private final UserRepository userRepository;
  private final String clientId;
  private final String clientSecret;
  private final String redirectUri;
  private final String jwksUrl = "https://kauth.kakao.com/.well-known/jwks.json";

  public KakaoAuthService(
      RestTemplate restTemplate,
      UserRepository userRepository,
      @Value("${kakao.client-id}") String clientId,
      @Value("${kakao.client-secret}") String clientSecret,
      @Value("${kakao.redirect-uri}") String redirectUri) {
    this.restTemplate = restTemplate;
    this.userRepository = userRepository;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.redirectUri = redirectUri;
  }

  public Map<String, Object> getToken(String code) {
    String tokenUrl = "https://kauth.kakao.com/oauth/token";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> params =
        new LinkedMultiValueMap<>() {
          {
            add("grant_type", "authorization_code");
            add("client_id", clientId);
            add("client_secret", clientSecret);
            add("code", code);
            add("redirect_uri", redirectUri);
          }
        };

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

    ResponseEntity<Map<String, Object>> response =
        restTemplate.exchange(
            tokenUrl,
            HttpMethod.POST,
            request,
            new ParameterizedTypeReference<Map<String, Object>>() {});

    if (response.getStatusCode().value() != 200) {
      throw new RuntimeException("Failed to get access token: " + response.getStatusCode());
    }

    Map<String, Object> tokenResponse = response.getBody();
    System.out.println("TOKEN: " + tokenResponse);
    return tokenResponse;
  }

  public Map<String, Object> validateOidcToken(String token) {
    try {
      System.out.println("Validating OIDC token with simplified JWKS approach...");

      // JWKS에서 공개키 가져오기
      ResponseEntity<Map<String, Object>> jwksResponse =
          restTemplate.exchange(
              jwksUrl,
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<Map<String, Object>>() {});

      Map<String, Object> jwks = jwksResponse.getBody();

      // 간단한 JWK Locator 생성
      Locator<Key> keyLocator = new JwkLocator(jwks);

      // JWT 파싱 및 검증
      Claims claims =
          Jwts.parser()
              .keyLocator(keyLocator)
              .requireAudience(clientId)
              .requireIssuer("https://kauth.kakao.com")
              .build()
              .parseSignedClaims(token)
              .getPayload();

      System.out.println("JWT validation successful!");
      System.out.println("DECODED CLAIMS: " + claims);

      return claims;
    } catch (Exception e) {
      System.out.println("JWT validation failed: " + e.getMessage());

      // JWT 검증 실패 시 예외를 던져서 실패를 명확히 알림
      throw new RuntimeException("Failed to verify OIDC token: " + e.getMessage(), e);
    }
  }

  public User processOidcToken(String token) {
    // OIDC 토큰 검증
    Map<String, Object> claims = validateOidcToken(token);
    
    // 사용자 정보 추출
    String email = (String) claims.get("email");
    String nickname = (String) claims.get("nickname");
    String picture = (String) claims.get("picture");
    String sub = (String) claims.get("sub");
    
    if (email == null) {
      throw new RuntimeException("Email is required for OIDC authentication");
    }
    
    // 1. provider + externalId로 먼저 찾기 (같은 Kakao 계정)
    User user = userRepository.findByProviderAndExternalId("kakao", sub)
        .orElseGet(() -> {
          // 2. 없으면 이메일로 찾기
          Optional<User> existingUser = userRepository.findByEmail(email);
          if (existingUser.isPresent()) {
            // 3. 이메일이 있지만 다른 provider로 가입된 경우 가입 거부
            User foundUser = existingUser.get();
            if (!"kakao".equals(foundUser.getProvider())) {
              throw new RuntimeException("Email already registered with different provider: " + foundUser.getProvider());
            }
            return foundUser;
          } else {
            // 4. 완전히 새로운 사용자 생성
            System.out.println("Creating new user: " + email + " " + nickname + " " + picture + " " + sub);
            return createNewUser(email, nickname, picture, "kakao", sub);
          }
        });
    
    // 사용자 정보 업데이트
    user.setUsername(nickname);
    user.setProfileImage(picture);
    user.setProvider("kakao");
    user.setExternalId(sub);
    
    return userRepository.save(user);
  }

  private User createNewUser(String email, String nickname, String picture, String provider, String externalId) {
    User newUser = new User();
    newUser.setEmail(email);
    newUser.setUsername(nickname);
    newUser.setProfileImage(picture);
    newUser.setProvider(provider);
    newUser.setExternalId(externalId);
    return userRepository.save(newUser);
  }

}
