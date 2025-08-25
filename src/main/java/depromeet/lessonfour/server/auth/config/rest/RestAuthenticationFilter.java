package depromeet.lessonfour.server.auth.config.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import depromeet.lessonfour.server.auth.api.dto.request.LoginRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import java.io.IOException;

public class RestAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

  private static final String LOGIN_URL = "/api/auth/login";

  private final ObjectMapper objectMapper;

  public RestAuthenticationFilter(
      AuthenticationManager authenticationManager,
      AuthenticationSuccessHandler successHandler,
      AuthenticationFailureHandler failureHandler,
      ObjectMapper objectMapper) {
    super(PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, LOGIN_URL));
    this.setAuthenticationManager(authenticationManager);
    this.setAuthenticationSuccessHandler(successHandler);
    this.setAuthenticationFailureHandler(failureHandler);
    this.objectMapper = objectMapper;
  }

  @Override
  public Authentication attemptAuthentication(
      HttpServletRequest request, HttpServletResponse response)
      throws AuthenticationException, IOException {
    LoginRequestDto dto = objectMapper.readValue(request.getReader(), LoginRequestDto.class);

    RestAuthenticationToken token = new RestAuthenticationToken(dto.email(), dto.password());

    return getAuthenticationManager().authenticate(token);
  }
}
