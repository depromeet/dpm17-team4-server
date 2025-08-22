package depromeet.lessonfour.server.common.security.rest;

import java.io.IOException;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import depromeet.lessonfour.server.common.security.rest.dto.LoginRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RestAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

  private static final String LOGIN_URL = "/api/auth/login";

  private final ObjectMapper objectMapper = new ObjectMapper();

  public RestAuthenticationFilter(
      AuthenticationManager authenticationManager,
      AuthenticationSuccessHandler successHandler,
      AuthenticationFailureHandler failureHandler) {
    super(
        request ->
            LOGIN_URL.equals(request.getServletPath())
                && HttpMethod.POST.matches(request.getMethod()));
    this.setAuthenticationManager(authenticationManager);
    this.setAuthenticationSuccessHandler(successHandler);
    this.setAuthenticationFailureHandler(failureHandler);
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
