package depromeet.lessonfour.server.auth.config.rest;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.auth.config.userdetails.AccountContext;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RestAuthenticationProvider implements AuthenticationProvider {

  private final UserDetailsService userDetailsService;
  private final PasswordEncoder passwordEncoder;

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {

    String loginId = authentication.getName();
    String password = (String) authentication.getCredentials();

    AccountContext userDetails = (AccountContext) userDetailsService.loadUserByUsername(loginId);

    validatePassword(password, userDetails);

    return new RestAuthenticationToken(userDetails.getAuthorities(), userDetails, null);
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return authentication.isAssignableFrom(RestAuthenticationToken.class);
  }

  private void validatePassword(String password, AccountContext userDetails) {
    if (!passwordEncoder.matches(password, userDetails.getPassword())) {
      throw new BadCredentialsException("Invalid password");
    }
  }
}
