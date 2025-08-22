package depromeet.lessonfour.server.common.security.rest;

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
import org.springframework.security.core.Authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class RestAuthenticationSuccessHandlerTest {

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Mock private Authentication authentication;

  private depromeet.lessonfour.server.common.security.rest.handler.RestAuthenticationSuccessHandler
      successHandler;
  private StringWriter stringWriter;
  private PrintWriter printWriter;

  @BeforeEach
  void setUp() throws Exception {
    successHandler =
        new depromeet.lessonfour.server.common.security.rest.handler
            .RestAuthenticationSuccessHandler();
    stringWriter = new StringWriter();
    printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);
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
}
