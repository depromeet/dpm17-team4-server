package depromeet.lessonfour.server.auth;

import java.security.Key;
import java.util.Base64;
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
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.JwtParserBuilder;
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
      // JWKS에서 공개키 가져오기
      System.out.println("Fetching JWKS from Kakao...");
      ResponseEntity<Map<String, Object>> jwksResponse =
          restTemplate.exchange(
              jwksUrl,
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<Map<String, Object>>() {});

      Map<String, Object> jwks = jwksResponse.getBody();
      System.out.println("JWKS fetched successfully");

      // JWT 라이브러리를 활용한 Locator 생성
      Locator<Key> keyLocator = new KakaoSigningKeyResolver(jwks);

      // JWT 파싱 및 검증 (JWT 라이브러리 활용)
      System.out.println("Parsing and validating JWT with JJWT library...");
      JwtParserBuilder parserBuilder = Jwts.parser();
      JwtParser parser =
          parserBuilder
              .keyLocator(keyLocator)
              .requireAudience(clientId)
              .requireIssuer("https://kauth.kakao.com")
              .build();

      Claims claims = parser.parseSignedClaims(token).getPayload();
      System.out.println("JWT validation successful!");
      System.out.println("DECODED CLAIMS: " + claims);
      return claims;

    } catch (Exception e) {
      System.out.println("JWT validation failed: " + e.getMessage());
      // Fallback: 간단한 Base64 디코딩
      try {
        System.out.println("Falling back to simple Base64 decode...");
        String[] tokenParts = token.split("\\.");
        String headerJson = new String(Base64.getUrlDecoder().decode(tokenParts[0]));
        String payloadJson = new String(Base64.getUrlDecoder().decode(tokenParts[1]));

        System.out.println("HEADER: " + headerJson);
        System.out.println("PAYLOAD: " + payloadJson);

        Map<String, Object> fallbackClaims =
            Map.of(
                "payload", payloadJson,
                "header", headerJson);
        // TODO: logging
        System.out.println("FALLBACK CLAIMS: " + fallbackClaims.toString());
      } catch (Exception fallbackException) {
        System.out.println("Fallback also failed: " + fallbackException.getMessage());
      }
      // TODO: remove e.getMessage() for security
      throw new RuntimeException("Failed to verify OIDC token: " + e.getMessage());
    }
  }
}
