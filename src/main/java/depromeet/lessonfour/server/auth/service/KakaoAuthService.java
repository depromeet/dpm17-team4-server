package depromeet.lessonfour.server.auth.service;

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

import depromeet.lessonfour.server.auth.api.dto.response.AuthResponseDto;
import depromeet.lessonfour.server.auth.api.dto.response.UserResponseDto;
import depromeet.lessonfour.server.auth.persist.jpa.UserRepository;
import depromeet.lessonfour.server.auth.persist.jpa.entity.User;
import depromeet.lessonfour.server.auth.security.jwt.JwkLocator;
import depromeet.lessonfour.server.auth.security.jwt.JwtTokenGenerator;
import depromeet.lessonfour.server.auth.security.userdetails.AccountContext;
import depromeet.lessonfour.server.auth.value.Provider;
import depromeet.lessonfour.server.auth.value.Provider.ProviderType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Locator;

@Service
public class KakaoAuthService {

  private final RestTemplate restTemplate;
  private final UserRepository userRepository;
  private final JwtTokenGenerator jwtTokenGenerator;
  private final String clientId;
  private final String clientSecret;
  private final String redirectUri;
  private final String issuerUrl;
  private final String tokenUrl;
  private final String jwksUrl = "https://kauth.kakao.com/.well-known/jwks.json";

  public KakaoAuthService(
      RestTemplate restTemplate,
      UserRepository userRepository,
      JwtTokenGenerator jwtTokenGenerator,
      @Value("${kakao.client-id}") String clientId,
      @Value("${kakao.client-secret}") String clientSecret,
      @Value("${kakao.redirect-url}") String redirectUrl,
      @Value("${kakao.issuer-url}") String issuerUrl,
      @Value("${kakao.token-url}") String tokenUrl) {
    this.restTemplate = restTemplate;
    this.userRepository = userRepository;
    this.jwtTokenGenerator = jwtTokenGenerator;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.redirectUri = redirectUrl;
    this.issuerUrl = issuerUrl;
    this.tokenUrl = tokenUrl;
  }

  public AuthResponseDto signin(String code) {
    if (code != null) {
      Map<String, Object> tokenData = getToken(code);
      String idToken = tokenData.get("id_token").toString();
      if (idToken == null) {
        throw new RuntimeException("Id token is required for Kakao signin");
      }

      User user = getUserFromToken(idToken);
      return AuthResponseDto.of(UserResponseDto.of(user), null, user.getRefreshToken());
    }
    throw new RuntimeException("Code is required for Kakao signin");
  }

  private Map<String, Object> getToken(String code) {
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
    return tokenResponse;
  }

  private Map<String, Object> validateOidcToken(String token) {
    try {
      ResponseEntity<Map<String, Object>> jwksResponse =
          restTemplate.exchange(
              jwksUrl,
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<Map<String, Object>>() {});

      Map<String, Object> jwks = jwksResponse.getBody();
      Locator<Key> keyLocator = new JwkLocator(jwks);
      Claims claims =
          Jwts.parser()
              .keyLocator(keyLocator)
              .requireAudience(clientId)
              .requireIssuer(issuerUrl)
              .build()
              .parseSignedClaims(token)
              .getPayload();
      return claims;
    } catch (Exception e) {
      throw new RuntimeException("Failed to verify OIDC token: " + e.getMessage(), e);
    }
  }

  private User getUserFromToken(String token) {
    Map<String, Object> claims = validateOidcToken(token);

    String email = (String) claims.get("email");
    String nickname = (String) claims.get("nickname");
    String picture = (String) claims.get("picture");
    String sub = (String) claims.get("sub");

    if (email == null) {
      throw new RuntimeException("Email is required for OIDC authentication");
    }

    Optional<User> existingUser = userRepository.findByEmail(email);

    if (existingUser.isPresent()) {
      // NOTE: 정보 업데이트? 우선 안함
      // TODO: 같은 provider인데 externalId가 다른 경우 (이론적으로는 발생하지 않아야 함)
      User user = existingUser.get();
      String refreshToken = jwtTokenGenerator.generateRefreshToken(AccountContext.of(user));
      user.storeRefreshToken(refreshToken);
      return userRepository.save(user);
    }

    User user =
        userRepository.save(
            User.register(email, nickname, null, picture, Provider.of(ProviderType.KAKAO, sub)));

    String refreshToken = jwtTokenGenerator.generateRefreshToken(AccountContext.of(user));
    user.storeRefreshToken(refreshToken);
    return userRepository.save(user);
  }
}
