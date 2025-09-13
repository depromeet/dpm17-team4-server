package depromeet.lessonfour.server.common.auth.service;

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
import org.springframework.web.util.UriComponentsBuilder;

import depromeet.lessonfour.server.common.auth.security.jwt.JwkLocator;
import depromeet.lessonfour.server.common.auth.security.jwt.JwtTokenGenerator;
import depromeet.lessonfour.server.common.auth.security.userdetails.AccountContext;
import depromeet.lessonfour.server.users.adapters.UserRepository;
import depromeet.lessonfour.server.users.domain.entities.User;
import depromeet.lessonfour.server.users.domain.values.Provider;
import depromeet.lessonfour.server.users.domain.values.Provider.ProviderType;
import depromeet.lessonfour.server.users.schemas.response.AuthResponseDto;
import depromeet.lessonfour.server.users.schemas.response.UserResponseDto;
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
  private final String authUrl;
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
      @Value("${kakao.token-url}") String tokenUrl,
      @Value("${kakao.auth-url}") String authUrl) {
    this.restTemplate = restTemplate;
    this.userRepository = userRepository;
    this.jwtTokenGenerator = jwtTokenGenerator;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.redirectUri = redirectUrl;
    this.issuerUrl = issuerUrl;
    this.tokenUrl = tokenUrl;
    this.authUrl = authUrl;
  }

  public AuthResponseDto login(String code) {
    if (code != null) {
      Map<String, Object> tokenData = getToken(code);
      String idToken = tokenData.get("id_token").toString();
      if (idToken == null) {
        throw new RuntimeException("Id token is required for Kakao login");
      }

      User user = getUserFromToken(idToken);
      return AuthResponseDto.of(UserResponseDto.of(user), null, user.getRefreshToken());
    }
    throw new RuntimeException("Code is required for Kakao login");
  }

  public String getRequestUrl() {
    MultiValueMap<String, String> authParams =
        new LinkedMultiValueMap<>() {
          {
            add("client_id", clientId);
            add("redirect_uri", redirectUri);
            add("response_type", "code");
            add("scope", "openid profile_nickname profile_image account_email");
          }
        };
    return UriComponentsBuilder.fromUriString(authUrl)
        .queryParams(authParams)
        .build()
        .toUriString();
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
