package depromeet.lessonfour.server.auth.app.service;

import java.util.Map;

import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;

import depromeet.lessonfour.server.auth.api.code.AuthErrorCode;
import depromeet.lessonfour.server.auth.app.client.UserServiceClient;
import depromeet.lessonfour.server.auth.app.dto.response.AuthResponseDto;
import depromeet.lessonfour.server.auth.app.dto.response.TokenPairDto;
import depromeet.lessonfour.server.auth.domain.vo.SocialProvider;
import depromeet.lessonfour.server.auth.infra.security.jwt.TokenManager;
import depromeet.lessonfour.server.auth.infra.security.oauth.OAuthTokenClient;
import depromeet.lessonfour.server.auth.infra.security.oauth.OidcTokenDecoder;
import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.common.exception.ServerException;
import depromeet.lessonfour.server.user.domain.entity.User;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class AuthCodeFlowUseCase {

  private final UserServiceClient userServiceClient;
  private final TokenManager tokenManager;
  private final OidcTokenDecoder oidcTokenDecoder;
  private final OAuthTokenClient oAuthTokenClient;

  public AuthResponseDto login(
      String code, SocialProvider socialProvider, boolean includeAccessToken) {
    OAuth2AccessTokenResponse response = oAuthTokenClient.requestToken(code, socialProvider);
    String idToken = (String) response.getAdditionalParameters().get("id_token");
    Map<String, Object> claims = oidcTokenDecoder.parseClaims(idToken);
    User user = extractAndFindUser(claims, socialProvider);

    TokenPairDto tokenPair = tokenManager.generateToken(user, includeAccessToken);

    return AuthResponseDto.of(user, tokenPair);
  }

  private User extractAndFindUser(Map<String, Object> claims, SocialProvider socialProvider) {
    String email = (String) claims.get("email");
    String nickname = (String) claims.get("nickname");
    String picture = (String) claims.get("picture");
    String sub = (String) claims.get("sub");

    if (email == null) {
      throw new ServerException(AuthErrorCode.EMAIL_REQUIRED_FOR_OIDC);
    }

    return userServiceClient.findOrCreate(email, nickname, picture, socialProvider, sub);
  }
}
