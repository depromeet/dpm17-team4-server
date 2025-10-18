package depromeet.lessonfour.server.auth.infra.security.oauth;

import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class OAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

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

    String stateValue =
        (stateParam != null && !stateParam.isEmpty())
            ? stateParam
            : oidcStateCodec.encode(null, null);

    return OAuth2AuthorizationRequest.from(req).state(stateValue).build();
  }
}
