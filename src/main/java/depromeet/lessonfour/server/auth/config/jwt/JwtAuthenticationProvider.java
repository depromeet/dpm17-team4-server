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
    isAuthenticationNull(authentication);

    final String token = authentication.getCredentials().toString();

    if (!jwtTokenValidator.isValidToken(token)) {
      throw new BadCredentialsException("Invalid JWT Token");
    }

    String email = jwtTokenValidator.extractEmail(token);
    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

    return new JwtAuthenticationToken(userDetails, userDetails.getAuthorities(), token);
  }

  private static void isAuthenticationNull(Authentication authentication) {
    if (authentication == null
        || authentication.getCredentials() == null
        || authentication.getCredentials().toString().isBlank()) {
      throw new BadCredentialsException("Missing JWT Token");
    }
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return JwtAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
