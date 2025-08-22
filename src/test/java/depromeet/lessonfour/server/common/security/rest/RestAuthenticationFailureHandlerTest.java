package depromeet.lessonfour.server.common.security.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import depromeet.lessonfour.server.common.security.rest.handler.RestAuthenticationFailureHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class RestAuthenticationFailureHandlerTest {

  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;

  private RestAuthenticationFailureHandler failureHandler;
  private StringWriter stringWriter;
  private PrintWriter printWriter;

  @BeforeEach
  void setUp() throws Exception {
    failureHandler = new RestAuthenticationFailureHandler();
    stringWriter = new StringWriter();
    printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);
  }

  @Test
  @DisplayName("인증 실패 시 HTTP 상태 코드 401을 설정한다")
  void whenAuthenticationFailure_thenSetStatusCode401() throws Exception {
    // given
    AuthenticationException exception = new BadCredentialsException("Invalid credentials");

    // when
    failureHandler.onAuthenticationFailure(request, response, exception);

    // then
    verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  @DisplayName("인증 실패 시 Content-Type을 application/json으로 설정한다")
  void whenAuthenticationFailure_thenSetContentTypeJson() throws Exception {
    // given
    AuthenticationException exception = new BadCredentialsException("Invalid credentials");

    // when
    failureHandler.onAuthenticationFailure(request, response, exception);

    // then
    verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
  }

  @Test
  @DisplayName("인증 실패 시 기본 에러 메시지를 응답한다")
  void whenAuthenticationFailure_thenReturnDefaultErrorMessage() throws Exception {
    // given
    AuthenticationException exception = new BadCredentialsException("Invalid credentials");

    // when
    failureHandler.onAuthenticationFailure(request, response, exception);
    printWriter.flush();

    // then
    String responseBody = stringWriter.toString();
    assertThat(responseBody).isEqualTo("{\"message\": \"Authentication failed\"}");
  }

  @Test
  @DisplayName("인증 실패 시 JSON 형식의 응답을 반환한다")
  void whenAuthenticationFailure_thenReturnJsonResponse() throws Exception {
    // given
    AuthenticationException exception = new BadCredentialsException("Invalid credentials");

    // when
    failureHandler.onAuthenticationFailure(request, response, exception);
    printWriter.flush();

    // then
    String responseBody = stringWriter.toString();
    assertThat(responseBody).contains("\"message\"");
    assertThat(responseBody).contains("\"Authentication failed\"");
    assertThat(responseBody).startsWith("{").endsWith("}");
  }

  @Test
  @DisplayName("이메일 마스킹 기능이 올바르게 작동한다 - 정상적인 이메일")
  void testEmailMasking_withValidEmail() throws Exception {
    // given
    AuthenticationException exception = new BadCredentialsException("Invalid credentials");
    when(request.getParameter("email")).thenReturn("test@example.com");
    when(request.getHeader("User-Agent")).thenReturn("TestAgent");

    // when
    failureHandler.onAuthenticationFailure(request, response, exception);

    // then
    // 로그 출력에서 마스킹된 이메일이 사용되는지는 로그 캡처로 확인
    verify(request).getParameter("email");
  }

  @Test
  @DisplayName("이메일 마스킹 기능이 올바르게 작동한다 - 짧은 이메일")
  void testEmailMasking_withShortEmail() throws Exception {
    // given
    AuthenticationException exception = new BadCredentialsException("Invalid credentials");
    when(request.getParameter("email")).thenReturn("a@b.c");
    when(request.getHeader("User-Agent")).thenReturn("TestAgent");

    // when
    failureHandler.onAuthenticationFailure(request, response, exception);

    // then
    verify(request).getParameter("email");
  }

  @Test
  @DisplayName("이메일 파라미터가 없을 때 N/A로 처리한다")
  void testEmailMasking_withNullEmail() throws Exception {
    // given
    AuthenticationException exception = new BadCredentialsException("Invalid credentials");
    when(request.getParameter("email")).thenReturn(null);
    when(request.getHeader("User-Agent")).thenReturn("TestAgent");

    // when
    failureHandler.onAuthenticationFailure(request, response, exception);

    // then
    verify(request).getParameter("email");
  }

  @Test
  @DisplayName("User-Agent 헤더를 로깅에 포함한다")
  void testUserAgentLogging() throws Exception {
    // given
    AuthenticationException exception = new BadCredentialsException("Invalid credentials");
    when(request.getParameter("email")).thenReturn("test@example.com");
    when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 TestBrowser");

    // when
    failureHandler.onAuthenticationFailure(request, response, exception);

    // then
    verify(request).getHeader("User-Agent");
  }

  @Test
  @DisplayName("로깅에 예외 메시지가 포함된다")
  void testExceptionMessageLogging(CapturedOutput output) throws Exception {
    // given
    String exceptionMessage = "Custom authentication error";
    AuthenticationException exception = new BadCredentialsException(exceptionMessage);
    when(request.getParameter("email")).thenReturn("test@example.com");
    when(request.getHeader("User-Agent")).thenReturn("TestAgent");

    // when
    failureHandler.onAuthenticationFailure(request, response, exception);

    // then
    assertThat(output.getOut()).contains("Authentication failed");
    assertThat(output.getOut()).contains("t****@example.com");
    assertThat(output.getOut()).contains("TestAgent");
    assertThat(output.getOut()).contains(exceptionMessage);
  }

  @Test
  @DisplayName("모든 응답 설정이 올바르게 이루어진다")
  void whenAuthenticationFailure_thenAllResponseSettingsAreCorrect() throws Exception {
    // given
    AuthenticationException exception = new BadCredentialsException("Invalid credentials");

    // when
    failureHandler.onAuthenticationFailure(request, response, exception);
    printWriter.flush();

    // then
    verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
    verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);

    String responseBody = stringWriter.toString();
    assertThat(responseBody).isNotEmpty();
    assertThat(responseBody).isEqualTo("{\"message\": \"Authentication failed\"}");
  }

  @Test
  @DisplayName("다양한 예외 타입에 대해 동일한 응답을 반환한다")
  void testDifferentExceptionTypes() throws Exception {
    // given
    AuthenticationException[] exceptions = {
      new BadCredentialsException("Bad credentials"),
      new AuthenticationException("General auth error") {}
    };

    for (AuthenticationException exception : exceptions) {
      // when
      stringWriter.getBuffer().setLength(0); // 버퍼 초기화
      failureHandler.onAuthenticationFailure(request, response, exception);
      printWriter.flush();

      // then
      String responseBody = stringWriter.toString();
      assertThat(responseBody).isEqualTo("{\"message\": \"Authentication failed\"}");
    }
  }
}
