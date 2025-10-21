package depromeet.lessonfour.server.auth.app.service;

import java.util.Map;

import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.auth.app.client.UserServiceClient;
import depromeet.lessonfour.server.auth.app.dto.response.AuthResponseDto;
import depromeet.lessonfour.server.auth.app.dto.response.TokenPairDto;
import depromeet.lessonfour.server.auth.domain.vo.UserInfo;
import depromeet.lessonfour.server.auth.infra.security.jwt.TokenManager;
import depromeet.lessonfour.server.auth.infra.security.oauth.OAuthTokenClient;
import depromeet.lessonfour.server.auth.infra.security.oauth.OidcTokenDecoder;
import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.user.domain.entity.User;
import depromeet.lessonfour.server.user.domain.vo.Provider;
import depromeet.lessonfour.server.user.domain.vo.Provider.ProviderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UseCase
@Transactional
@RequiredArgsConstructor
public class AuthCodeFlowUseCase {

  private final UserServiceClient userServiceClient;
  private final TokenManager tokenManager;
  private final OidcTokenDecoder oidcTokenDecoder;
  private final OAuthTokenClient oAuthTokenClient;
  private final AppleUserCache appleUserCache;

  public AuthResponseDto login(String code, String provider, boolean includeAccessToken) {
    ProviderType providerType = ProviderType.from(provider);
    OAuth2AccessTokenResponse response = oAuthTokenClient.requestToken(code, providerType);
    String idToken = (String) response.getAdditionalParameters().get("id_token");
    UserInfo info = oidcTokenDecoder.decode(idToken, provider);

    String nickname = info.getNickname();
    if (providerType == ProviderType.APPLE) {
      Map<String, Object> appleUser = appleUserCache.getUser(code);
      if (appleUser != null) {
        String appleNickname = extractAppleNickname(appleUser);
        if (appleNickname != null) {
          nickname = appleNickname;
          log.info("Using Apple cached name: {}", nickname);
        }
      }
    }

    User user =
        userServiceClient.findOrCreate(
            info.getEmail(), nickname, info.getPicture(), Provider.of(providerType, info.getSub()));

    TokenPairDto tokenPair = tokenManager.generateAndStoreTokens(user, includeAccessToken);

    return AuthResponseDto.of(user, tokenPair);
  }

  private String extractAppleNickname(Map<String, Object> appleUser) {
    Object nameObj = appleUser.get("name");
    if (nameObj instanceof Map) {
      @SuppressWarnings("unchecked")
      Map<String, Object> name = (Map<String, Object>) nameObj;
      String firstName = (String) name.get("firstName");
      String lastName = (String) name.get("lastName");

      if (firstName != null && lastName != null) {
        return firstName + " " + lastName;
      } else if (firstName != null) {
        return firstName;
      } else if (lastName != null) {
        return lastName;
      }
    }
    return null;
  }
}
