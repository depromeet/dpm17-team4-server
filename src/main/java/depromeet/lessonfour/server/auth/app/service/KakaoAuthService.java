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
import org.springframework.web.client.ResourceAccessException;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
    log.info(
        "[KakaoAuthService] Starting login process with code: {}",
        code != null ? "present" : "null");
    if (code != null) {
      try {
        log.debug("[KakaoAuthService] Requesting token from Kakao");
        Map<String, Object> tokenData = getToken(code);
        log.debug("[KakaoAuthService] Token response received from Kakao: {}", tokenData.keySet());

        String idToken = tokenData.get("id_token").toString();
        if (idToken == null) {
          log.error("[KakaoAuthService] ID token not found in Kakao response");
          throw new ServerException(AuthErrorCode.ID_TOKEN_REQUIRED);
        }
        log.info("[KakaoAuthService] ID token received successfully");

        log.debug("[KakaoAuthService] Processing user from ID token");
        User user = getUserFromToken(idToken);
        log.info(
            "[KakaoAuthService] User processed successfully - userId: {}, email: {}",
            user.getId(),
            user.getEmail());

        return AuthResponseDto.of(UserResponseDto.of(user), null, user.getRefreshToken());
      } catch (Exception e) {
        log.error("[KakaoAuthService] Login process failed: {}", e.getMessage(), e);
        throw e;
      }
    }
    log.error("[KakaoAuthService] Authorization code is required but was null");
    throw new ServerException(AuthErrorCode.LOGIN_CODE_REQUIRED);
  }

  public String getRequestUrl(String clientRedirectUri) {
    MultiValueMap<String, String> authParams =
        new LinkedMultiValueMap<>() {
          {
            add("client_id", clientId);
            add("redirect_uri", redirectUri); // serverRedirectUri
            add("state", clientRedirectUri);
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
    log.debug("[KakaoAuthService] Preparing token request to Kakao - tokenUrl: {}", tokenUrl);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> params =
        new LinkedMultiValueMap<>() {
          {
            add("grant_type", "authorization_code");
            add("client_id", clientId);
            add("client_secret", "[HIDDEN]");
            add("code", code);
            add("redirect_uri", redirectUri);
          }
        };
    log.debug(
        "[KakaoAuthService] Token request parameters prepared - client_id: {}, redirect_uri: {}",
        clientId,
        redirectUri);

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

    try {
      log.info("[KakaoAuthService] Sending token request to Kakao");
      ResponseEntity<Map<String, Object>> response =
          restTemplate.exchange(
              tokenUrl,
              HttpMethod.POST,
              request,
              new ParameterizedTypeReference<Map<String, Object>>() {});

      log.info("[KakaoAuthService] Token response received - status: {}", response.getStatusCode());
      if (response.getStatusCode().value() != 200) {
        log.error(
            "[KakaoAuthService] Token request failed with status: {}", response.getStatusCode());
        throw new ServerException(AuthErrorCode.OAUTH_TOKEN_REQUEST_FAILED);
      }

      Map<String, Object> tokenResponse = response.getBody();
      log.debug(
          "[KakaoAuthService] Token response body keys: {}",
          tokenResponse != null ? tokenResponse.keySet() : "null");
      return tokenResponse;
    } catch (org.springframework.web.client.ResourceAccessException e) {
      log.error(
          "[KakaoAuthService] Network connection failed during token request: {}", e.getMessage());
      if (e.getCause() instanceof javax.net.ssl.SSLException) {
        log.error("[KakaoAuthService] SSL connection error detected - check HTTPS configuration");
      }
      throw new ServerException(AuthErrorCode.OAUTH_TOKEN_REQUEST_FAILED);
    } catch (Exception e) {
      log.error("[KakaoAuthService] Exception during token request: {}", e.getMessage(), e);
      throw new ServerException(AuthErrorCode.OAUTH_TOKEN_REQUEST_FAILED);
    }
  }

  private Map<String, Object> validateOidcToken(String token) {
    log.debug("[KakaoAuthService] Starting OIDC token validation");
    try {
      log.debug("[KakaoAuthService] Fetching JWKS from: {}", jwksUrl);
      ResponseEntity<Map<String, Object>> jwksResponse =
          restTemplate.exchange(
              jwksUrl,
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<Map<String, Object>>() {});
      log.debug(
          "[KakaoAuthService] JWKS response received - status: {}", jwksResponse.getStatusCode());

      Map<String, Object> jwks = jwksResponse.getBody();
      log.debug("[KakaoAuthService] Creating key locator from JWKS");
      Locator<Key> keyLocator = new JwkLocator(jwks);

      log.debug(
          "[KakaoAuthService] Parsing and validating ID token with audience: {}, issuer: {}",
          clientId,
          issuerUrl);
      Claims claims =
          Jwts.parser()
              .keyLocator(keyLocator)
              .requireAudience(clientId)
              .requireIssuer(issuerUrl)
              .build()
              .parseSignedClaims(token)
              .getPayload();
      log.info(
          "[KakaoAuthService] OIDC token validation successful - subject: {}", claims.getSubject());
      return claims;
    } catch (ResourceAccessException e) {
      log.error(
          "[KakaoAuthService] Network connection failed during JWKS fetch: {}", e.getMessage());
      if (e.getCause() instanceof javax.net.ssl.SSLException) {
        log.error(
            "[KakaoAuthService] SSL error detected while fetching JWKS - check HTTPS configuration");
      }
      throw new ServerException(AuthErrorCode.INVALID_OIDC_TOKEN);
    } catch (Exception e) {
      log.error("[KakaoAuthService] OIDC token validation failed: {}", e.getMessage(), e);
      throw new ServerException(AuthErrorCode.INVALID_OIDC_TOKEN);
    }
  }

  private User getUserFromToken(String token) {
    log.debug("[KakaoAuthService] Extracting user information from token");
    Map<String, Object> claims = validateOidcToken(token);

    String email = (String) claims.get("email");
    String nickname = (String) claims.get("nickname");
    String picture = (String) claims.get("picture");
    String sub = (String) claims.get("sub");
    log.debug(
        "[KakaoAuthService] Claims extracted - email: {}, nickname: {}, sub: {}, picture present: {}",
        email,
        nickname,
        sub,
        picture != null);

    if (email == null) {
      log.error("[KakaoAuthService] Email claim is missing from OIDC token");
      throw new ServerException(AuthErrorCode.EMAIL_REQUIRED_FOR_OIDC);
    }

    log.debug("[KakaoAuthService] Looking up existing user by email: {}", email);
    Optional<User> existingUser = userRepository.findByEmail(email);

    if (existingUser.isPresent()) {
      log.info("[KakaoAuthService] Existing user found - userId: {}", existingUser.get().getId());
      // NOTE: 정보 업데이트? 우선 안함
      // TODO: 같은 provider인데 externalId가 다른 경우 (이론적으로는 발생하지 않아야 함)
      User user = existingUser.get();
      log.debug("[KakaoAuthService] Generating new refresh token for existing user");
      String refreshToken = jwtTokenGenerator.generateRefreshToken(AccountContext.of(user));
      user.storeRefreshToken(refreshToken);
      User savedUser = userRepository.save(user);
      log.info(
          "[KakaoAuthService] Existing user login completed successfully - userId: {}",
          savedUser.getId());
      return savedUser;
    }

    log.info("[KakaoAuthService] Creating new user with email: {}", email);
    User user =
        userRepository.save(
            User.register(email, nickname, null, picture, Provider.of(ProviderType.KAKAO, sub)));
    log.info("[KakaoAuthService] New user created - userId: {}", user.getId());

    log.debug("[KakaoAuthService] Generating refresh token for new user");
    String refreshToken = jwtTokenGenerator.generateRefreshToken(AccountContext.of(user));
    user.storeRefreshToken(refreshToken);
    User savedUser = userRepository.save(user);
    log.info(
        "[KakaoAuthService] New user registration completed successfully - userId: {}",
        savedUser.getId());
    return savedUser;
  }
}
