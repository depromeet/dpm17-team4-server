package depromeet.lessonfour.server.common.security.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import depromeet.lessonfour.server.auth.config.jwt.JwtTokenGenerator;
import depromeet.lessonfour.server.auth.config.rest.handler.RestAuthenticationSuccessHandler;
import depromeet.lessonfour.server.auth.config.userdetails.AccountContext;
import depromeet.lessonfour.server.auth.service.UserUpdateService;
import depromeet.lessonfour.server.common.utils.HttpServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class RestAuthenticationSuccessHandlerTest {

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Mock private Authentication authentication;

  @Mock private JwtTokenGenerator jwtTokenGenerator;

  @Mock private HttpServletUtils httpServletUtils;

  @Mock private UserUpdateService userUpdateService;

  @Mock private AccountContext accountContext;

  private RestAuthenticationSuccessHandler successHandler;
  private StringWriter stringWriter;
  private PrintWriter printWriter;

  @BeforeEach
  void setUp() throws Exception {
    successHandler =
        new RestAuthenticationSuccessHandler(
            jwtTokenGenerator, httpServletUtils, userUpdateService);
    stringWriter = new StringWriter();
    printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);
    when(authentication.getPrincipal()).thenReturn(accountContext);
    when(jwtTokenGenerator.generateAccessToken(any(AccountContext.class)))
        .thenReturn("mock-access-token");
    when(jwtTokenGenerator.generateRefreshToken(any(AccountContext.class)))
        .thenReturn("mock-refresh-token");
  }

  @Test
  @DisplayName("성공적인 인증 시 HTTP 상태 코드 200을 설정한다")
  void whenAuthenticationSuccess_thenSetStatusCode200() throws Exception {
    // when
    successHandler.onAuthenticationSuccess(request, response, authentication);

    // then
    verify(response).setStatus(HttpServletResponse.SC_OK);
  }

  @Test
  @DisplayName("성공적인 인증 시 Content-Type을 application/json으로 설정한다")
  void whenAuthenticationSuccess_thenSetContentTypeJson() throws Exception {
    // when
    successHandler.onAuthenticationSuccess(request, response, authentication);

    // then
    verify(response).setContentType("application/json");
  }

  @Test
  @DisplayName("성공적인 인증 시 Access Token을 생성하고 응답 body에 포함한다")
  void whenAuthenticationSuccess_thenGenerateAccessTokenAndIncludeInResponse() throws Exception {
    // when
    successHandler.onAuthenticationSuccess(request, response, authentication);

    // then
    verify(jwtTokenGenerator).generateAccessToken(accountContext);
    printWriter.flush();
    String responseBody = stringWriter.toString();
    assert responseBody.contains("mock-access-token");
  }

  @Test
  @DisplayName("성공적인 인증 시 Refresh Token을 생성하고 쿠키에 저장한다")
  void whenAuthenticationSuccess_thenGenerateRefreshTokenAndSetCookie() throws Exception {
    // when
    successHandler.onAuthenticationSuccess(request, response, authentication);

    // then
    verify(jwtTokenGenerator).generateRefreshToken(accountContext);
    verify(httpServletUtils)
        .addCookie(
            eq(response), eq("refreshToken"), eq("mock-refresh-token"), eq(Duration.ofDays(7)));
  }
}
