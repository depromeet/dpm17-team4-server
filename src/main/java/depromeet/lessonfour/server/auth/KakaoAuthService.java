package depromeet.lessonfour.server.auth;

import java.security.Key;
import java.util.Map;

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

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Locator;

@Service
public class KakaoAuthService {

  private final RestTemplate restTemplate;
  private final String clientId;
  private final String clientSecret;
  private final String redirectUri;
  private final String jwksUrl = "https://kauth.kakao.com/.well-known/jwks.json";

  public KakaoAuthService(
      RestTemplate restTemplate,
      @Value("${kakao.client-id}") String clientId,
      @Value("${kakao.client-secret}") String clientSecret,
      @Value("${kakao.redirect-uri}") String redirectUri) {
    this.restTemplate = restTemplate;
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
}
