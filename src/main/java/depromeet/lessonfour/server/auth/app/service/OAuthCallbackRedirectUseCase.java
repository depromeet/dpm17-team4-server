package depromeet.lessonfour.server.auth.app.service;

import java.nio.charset.StandardCharsets;

import org.springframework.web.util.UriComponentsBuilder;

import depromeet.lessonfour.server.auth.api.code.AuthErrorCode;
import depromeet.lessonfour.server.auth.domain.vo.StateData;
import depromeet.lessonfour.server.auth.infra.security.oauth.OidcStateCodec;
import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.common.exception.ServerException;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class OAuthCallbackRedirectUseCase {

  private final OidcStateCodec oidcStateCodec;

  public String getPath(String provider, String code, String state, String error) {
    if (error != null) {
      throw new ServerException(AuthErrorCode.OAUTH_TOKEN_REQUEST_FAILED);
    }

    StateData decodedState = oidcStateCodec.decode(state);

    // auth flow 진행
    if ("code".equalsIgnoreCase(decodedState.responseType())) {
      return UriComponentsBuilder.fromUriString(decodedState.redirectUri())
          .queryParam("code", code)
          .encode(StandardCharsets.UTF_8)
          .build()
          .toUriString();
    }

    // token flow 진행 - provider에 따라 동적으로 path 생성
    return UriComponentsBuilder.fromPath(String.format("/login/oauth2/code/%s", provider))
        .queryParam("code", code)
        .queryParam("state", state)
        .encode(StandardCharsets.UTF_8)
        .build()
        .toUriString();
  }
}
