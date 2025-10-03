package depromeet.lessonfour.server.auth.domain;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.auth.domain.vo.AccountContext;
import depromeet.lessonfour.server.user.app.service.UserQueryService;
import depromeet.lessonfour.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("userDetailsService")
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserQueryService userQueryService;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user = userQueryService.getUserForAuthentication(email);
    return AccountContext.of(user);
  }
}
