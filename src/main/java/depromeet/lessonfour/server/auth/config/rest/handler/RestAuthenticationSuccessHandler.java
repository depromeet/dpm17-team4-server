package depromeet.lessonfour.server.auth.config.rest.handler;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import depromeet.lessonfour.server.auth.config.jwt.JwtTokenGenerator;
import depromeet.lessonfour.server.auth.config.userdetails.AccountContext;
import depromeet.lessonfour.server.auth.service.UserUpdateService;
import depromeet.lessonfour.server.common.utils.HttpServletUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RestAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  private static final Duration REFRESH_TOKEN_EXPIRATION = Duration.ofDays(7);

  private final JwtTokenGenerator jwtTokenGenerator;
  private final HttpServletUtils httpServletUtils;
  private final UserUpdateService userUpdateService;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException {

    AccountContext accountContext = (AccountContext) authentication.getPrincipal();

    // JWT 토큰 생성
    String accessToken = jwtTokenGenerator.generateAccessToken(accountContext);
    String refreshToken = jwtTokenGenerator.generateRefreshToken(accountContext);

    // Refresh token을 DB에 저장
    userUpdateService.updateRefreshToken(accountContext.getId(), refreshToken);

    // Refresh token을 HttpOnly 쿠키에 저장
    httpServletUtils.addCookie(response, "refreshToken", refreshToken, REFRESH_TOKEN_EXPIRATION);

    // Access token을 JSON 응답 body에 포함
    Map<String, Object> responseBody = Map.of("accessToken", accessToken);

    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().write(objectMapper.writeValueAsString(responseBody));
  }
}
