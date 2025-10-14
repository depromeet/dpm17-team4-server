package depromeet.lessonfour.server.auth.infra.security.oauth;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import depromeet.lessonfour.server.auth.api.util.RefreshTokenCookieGenerator;
import depromeet.lessonfour.server.auth.app.dto.response.AuthResponseDto;
import depromeet.lessonfour.server.auth.domain.vo.StateData;
import depromeet.lessonfour.server.auth.infra.security.jwt.TokenManager;
import depromeet.lessonfour.server.common.util.UriUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final DelegatingOAuthLoginService delegatingOAuthLoginService;
  private final TokenManager tokenManager;
  private final OidcStateCodec oidcStateCodec;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {

    OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
    OAuth2User oAuth2User = token.getPrincipal();
    StateData stateData = oidcStateCodec.decode(request.getParameter("state"));

    AuthResponseDto authResponse =
        delegatingOAuthLoginService.login(token.getAuthorizedClientRegistrationId(), oAuth2User);

    String refreshToken = tokenManager.generateAndStoreTokens(authResponse);
    setCookie(response, UriUtils.extractDomain(stateData.redirectUri()), refreshToken);

    redirectWithUserInfo(response, stateData.redirectUri(), authResponse);
  }

  private void setCookie(HttpServletResponse response, String domain, String refreshToken) {
    ResponseCookie cookie =
        (domain != null && !domain.isBlank())
            ? RefreshTokenCookieGenerator.generate(refreshToken, domain)
            : RefreshTokenCookieGenerator.generate(refreshToken);

    response.addHeader("Set-Cookie", cookie.toString());

    // same-domain 쿠키도 추가
    if (domain != null && !domain.isBlank()) {
      ResponseCookie localCookie = RefreshTokenCookieGenerator.generate(refreshToken);
      response.addHeader("Set-Cookie", localCookie.toString());
    }
  }

  private void redirectWithUserInfo(
      HttpServletResponse response, String redirectUri, AuthResponseDto userInfo)
      throws IOException {
    String targetUri =
        UriComponentsBuilder.fromUriString(redirectUri)
            .queryParam("id", userInfo.id())
            .queryParam("nickname", userInfo.nickname())
            .queryParam("profileImage", userInfo.profileImage())
            .queryParam("isNew", userInfo.isNew())
            .queryParam("providerType", userInfo.provider())
            .encode(StandardCharsets.UTF_8)
            .build()
            .toUriString();

    response.sendRedirect(targetUri);
  }
}
