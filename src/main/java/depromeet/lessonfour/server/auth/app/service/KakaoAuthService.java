package depromeet.lessonfour.server.auth.app.service;

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

import depromeet.lessonfour.server.auth.api.code.AuthErrorCode;
import depromeet.lessonfour.server.auth.domain.vo.AccountContext;
import depromeet.lessonfour.server.auth.infra.security.jwt.JwkLocator;
import depromeet.lessonfour.server.auth.infra.security.jwt.JwtTokenGenerator;
import depromeet.lessonfour.server.common.exception.ServerException;
import depromeet.lessonfour.server.user.app.dto.response.AuthResponseDto;
import depromeet.lessonfour.server.user.app.dto.response.UserResponseDto;
import depromeet.lessonfour.server.user.domain.entity.User;
import depromeet.lessonfour.server.user.domain.vo.Provider;
import depromeet.lessonfour.server.user.domain.vo.Provider.ProviderType;
import depromeet.lessonfour.server.user.infra.repository.UserRepository;
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
        throw new ServerException(AuthErrorCode.ID_TOKEN_REQUIRED);
      }

      User user = getUserFromToken(idToken);
      return AuthResponseDto.of(UserResponseDto.of(user), null, user.getRefreshToken());
    }
    throw new ServerException(AuthErrorCode.LOGIN_CODE_REQUIRED);
  }

  public String getRequestUrl(String clientRedirectUri, String responseType) {
    // state에 redirectUri와 responseType을 함께 인코딩
    // redirectUri가 null이면 빈 문자열로 처리
    String safeRedirectUri = (clientRedirectUri != null && !clientRedirectUri.isBlank()) 
        ? clientRedirectUri : "";
    
    final String stateValue;
    if (responseType != null && !responseType.isBlank()) {
      // responseType이 있는 경우: "redirectUri|responseType=value" 형태
      stateValue = safeRedirectUri + "|responseType=" + responseType;
    } else {
      // responseType이 없는 경우: redirectUri만 또는 빈 문자열
      stateValue = safeRedirectUri;
    }
    
    MultiValueMap<String, String> authParams =
        new LinkedMultiValueMap<>() {
          {
            add("client_id", clientId);
            add("redirect_uri", redirectUri); // serverRedirectUri
            add("state", stateValue);
            add("response_type", "code");
            add("scope", "openid profile_nickname profile_image account_email");
          }
        };
    return UriComponentsBuilder.fromUriString(authUrl)
        .queryParams(authParams)
        .build()
        .toUriString();
  }
  
  public String getRequestUrl(String clientRedirectUri) {
    return getRequestUrl(clientRedirectUri, null);
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
      throw new ServerException(AuthErrorCode.OAUTH_TOKEN_REQUEST_FAILED);
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
      throw new ServerException(AuthErrorCode.INVALID_OIDC_TOKEN);
    }
  }

  private User getUserFromToken(String token) {
    Map<String, Object> claims = validateOidcToken(token);

    String email = (String) claims.get("email");
    String nickname = (String) claims.get("nickname");
    String picture = (String) claims.get("picture");
    String sub = (String) claims.get("sub");

    if (email == null) {
      throw new ServerException(AuthErrorCode.EMAIL_REQUIRED_FOR_OIDC);
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
