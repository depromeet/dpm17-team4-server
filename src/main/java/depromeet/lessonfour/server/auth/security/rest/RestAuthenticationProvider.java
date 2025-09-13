package depromeet.lessonfour.server.auth.security.rest;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.auth.security.userdetails.AccountContext;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RestAuthenticationProvider implements AuthenticationProvider {

  private final UserDetailsService userDetailsService;
  private final PasswordEncoder passwordEncoder;

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {

    if (authentication == null
        || authentication.getCredentials() == null
        || ((String) authentication.getCredentials()).isBlank()) {
      throw new BadCredentialsException("Invalid credentials");
    }

    String loginId = authentication.getName();
    String password = (String) authentication.getCredentials();

    AccountContext userDetails = (AccountContext) userDetailsService.loadUserByUsername(loginId);

    validatePassword(password, userDetails);

    return new RestAuthenticationToken(userDetails.getAuthorities(), userDetails, null);
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return RestAuthenticationToken.class.isAssignableFrom(authentication);
  }

  private void validatePassword(String password, AccountContext userDetails) {
    if (!passwordEncoder.matches(password, userDetails.getPassword())) {
      throw new BadCredentialsException("Invalid password");
    }
  }
}
