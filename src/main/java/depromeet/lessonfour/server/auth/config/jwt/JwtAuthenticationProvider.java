package depromeet.lessonfour.server.auth.config.jwt;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {

  private final JwtTokenValidator jwtTokenValidator;
  private final UserDetailsService userDetailsService;

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    String token = (String) authentication.getCredentials();

    if (!jwtTokenValidator.isValidToken(token)) {
      throw new BadCredentialsException("Invalid JWT Token");
    }

    String email = jwtTokenValidator.extractEmail(token);
    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

    return new JwtAuthenticationToken(userDetails, userDetails.getAuthorities(), token);
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return JwtAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
