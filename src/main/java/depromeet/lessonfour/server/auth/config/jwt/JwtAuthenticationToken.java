package depromeet.lessonfour.server.auth.config.jwt;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

  private final Object principal;
  private final String token;

  // 인증 전
  public JwtAuthenticationToken(String token) {
    super(null);
    this.principal = null;
    this.token = token;
    setAuthenticated(false);
  }

  // 인증 후
  public JwtAuthenticationToken(
      Object principal, Collection<? extends GrantedAuthority> authorities, String token) {
    super(authorities);
    this.principal = principal;
    this.token = token;
    setAuthenticated(true);
  }

  @Override
  public Object getCredentials() {
    return token; // 자격 증명으로 토큰 문자열을 사용
  }

  @Override
  public Object getPrincipal() {
    return principal; // 인증된 사용자 정보
  }
}
