package depromeet.lessonfour.server.auth.security.rest;

import java.io.IOException;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;

import depromeet.lessonfour.server.auth.api.dto.request.LoginRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
    try {
      LoginRequestDto dto = objectMapper.readValue(request.getInputStream(), LoginRequestDto.class);
      RestAuthenticationToken token = new RestAuthenticationToken(dto.email(), dto.password());
      token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      return getAuthenticationManager().authenticate(token);
    } catch (IOException e) {
      throw new AuthenticationServiceException("Invalid login payload", e);
    }
  }
}
