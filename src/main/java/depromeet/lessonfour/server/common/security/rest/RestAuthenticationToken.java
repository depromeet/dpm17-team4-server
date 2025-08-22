package depromeet.lessonfour.server.common.security.rest;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class RestAuthenticationToken extends AbstractAuthenticationToken {

  private final Object principal;
  private final Object credentials;

  // 인증된 토큰
  public RestAuthenticationToken(
      Collection<? extends GrantedAuthority> authorities, Object principal, Object credentials) {
    super(authorities);
    this.principal = principal;
    this.credentials = credentials;
    setAuthenticated(true);
  }

  // 인증되지 않은 토큰
  public RestAuthenticationToken(Object principal, Object credentials) {
    super(null);
    this.principal = principal;
    this.credentials = credentials;
    setAuthenticated(false);
  }

  @Override
  public Object getCredentials() {
    return credentials;
  }

  @Override
  public Object getPrincipal() {
    return principal;
  }
}
