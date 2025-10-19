package depromeet.lessonfour.server.auth.infra.security.oauth;

import java.util.HashMap;
import java.util.Map;

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
    String registrationId = extractRegistrationId(request);
    return customize(req, request, registrationId);
  }

  @Override
  public OAuth2AuthorizationRequest resolve(
      HttpServletRequest request, String clientRegistrationId) {
    OAuth2AuthorizationRequest req = defaultResolver.resolve(request, clientRegistrationId);
    return customize(req, request, clientRegistrationId);
  }

  private OAuth2AuthorizationRequest customize(
      OAuth2AuthorizationRequest req, HttpServletRequest request, String clientRegistrationId) {
    if (req == null) return null;

    // state 파라미터 처리
    String stateParam = request.getParameter("state");
    String stateValue =
        (stateParam != null && !stateParam.isEmpty())
            ? stateParam
            : oidcStateCodec.encode(null, null);

    OAuth2AuthorizationRequest.Builder builder =
        OAuth2AuthorizationRequest.from(req).state(stateValue);

    // Apple OAuth의 경우 response_mode=form_post 추가
    if ("apple".equalsIgnoreCase(clientRegistrationId)) {
      Map<String, Object> additionalParameters = new HashMap<>(req.getAdditionalParameters());
      additionalParameters.put("response_mode", "form_post");
      builder.additionalParameters(additionalParameters);
    }

    return builder.build();
  }

  private String extractRegistrationId(HttpServletRequest request) {
    String uri = request.getRequestURI();
    int lastSlash = uri.lastIndexOf('/');
    return (lastSlash != -1) ? uri.substring(lastSlash + 1) : null;
  }
}
