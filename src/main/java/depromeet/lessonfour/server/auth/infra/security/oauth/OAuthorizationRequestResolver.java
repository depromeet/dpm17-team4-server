package depromeet.lessonfour.server.auth.infra.security.oauth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class OAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

  @Value("${frontend.url}")
  private String defaultRedirectUri;

  private final OAuth2AuthorizationRequestResolver defaultResolver;
  private final OidcStateCodec oidcStateCodec;

  public OAuthorizationRequestResolver(
      ClientRegistrationRepository repo, OidcStateCodec oidcStateCodec) {
    this.defaultResolver =
        new DefaultOAuth2AuthorizationRequestResolver(repo, "/oauth2/authorization");
    this.oidcStateCodec = oidcStateCodec;
  }

  @Override
  public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
    OAuth2AuthorizationRequest req = defaultResolver.resolve(request);
    return customize(req, request);
  }

  @Override
  public OAuth2AuthorizationRequest resolve(
      HttpServletRequest request, String clientRegistrationId) {
    OAuth2AuthorizationRequest req = defaultResolver.resolve(request, clientRegistrationId);
    return customize(req, request);
  }

  private OAuth2AuthorizationRequest customize(
      OAuth2AuthorizationRequest req, HttpServletRequest request) {
    if (req == null) return null;

    String stateParam = request.getParameter("state");

    String stateValue;
    if (stateParam != null && !stateParam.isEmpty()) {
      // state가 이미 인코딩되어 있으면 그대로 사용
      stateValue = stateParam;
    } else {
      // state가 없으면 default 값으로 인코딩
      stateValue = oidcStateCodec.encode(defaultRedirectUri, "");
    }

    return OAuth2AuthorizationRequest.from(req).state(stateValue).build();
  }
}
