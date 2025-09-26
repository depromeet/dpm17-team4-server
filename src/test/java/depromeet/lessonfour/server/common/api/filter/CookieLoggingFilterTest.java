package depromeet.lessonfour.server.common.api.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;

@ExtendWith(MockitoExtension.class)
class CookieLoggingFilterTest {

  private CookieLoggingFilter cookieLoggingFilter;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  private ListAppender<ILoggingEvent> logAppender;

  @Mock private FilterChain filterChain;

  @BeforeEach
  void setUp() {
    cookieLoggingFilter = new CookieLoggingFilter();
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();

    // 로그 캡처를 위한 설정
    Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(CookieLoggingFilter.class);
    logAppender = new ListAppender<>();
    logAppender.start();
    logger.addAppender(logAppender);
    logger.setLevel(Level.INFO);
  }

  @Test
  @DisplayName("쿠키가 없는 경우 DEBUG 레벨로 로그 출력")
  void logNoCookies() throws ServletException, IOException {
    // given
    request.setMethod("GET");
    request.setRequestURI("/api/v1/test");

    // when
    cookieLoggingFilter.doFilterInternal(request, response, filterChain);

    // then
    verify(filterChain).doFilter(request, response);
    // DEBUG 레벨이므로 INFO 레벨에서는 로그가 캡처되지 않음
    assertThat(logAppender.list).isEmpty();
  }

  @Test
  @DisplayName("일반 쿠키가 있는 경우 INFO 레벨로 로그 출력")
  void logNormalCookies() throws ServletException, IOException {
    // given
    request.setMethod("POST");
    request.setRequestURI("/api/v1/users");
    request.setCookies(new Cookie("username", "testuser"), new Cookie("theme", "dark"));

    // when
    cookieLoggingFilter.doFilterInternal(request, response, filterChain);

    // then
    verify(filterChain).doFilter(request, response);
    assertThat(logAppender.list).hasSize(1);
    ILoggingEvent logEvent = logAppender.list.get(0);
    assertThat(logEvent.getLevel()).isEqualTo(Level.INFO);
    assertThat(logEvent.getFormattedMessage())
        .contains("[CookieLog] Request: POST /api/v1/users")
        .contains("username=testuser")
        .contains("theme=dark");
  }

  @Test
  @DisplayName("민감한 쿠키는 마스킹 처리")
  void maskSensitiveCookies() throws ServletException, IOException {
    // given
    request.setMethod("GET");
    request.setRequestURI("/api/v1/profile");
    request.setCookies(
        new Cookie("access_token", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"),
        new Cookie("session_id", "abc123def456"),
        new Cookie("JSESSIONID", "F1234567890ABCDEF"),
        new Cookie("normal_cookie", "normal_value"));

    // when
    cookieLoggingFilter.doFilterInternal(request, response, filterChain);

    // then
    verify(filterChain).doFilter(request, response);
    assertThat(logAppender.list).hasSize(1);
    ILoggingEvent logEvent = logAppender.list.get(0);
    String logMessage = logEvent.getFormattedMessage();

    assertThat(logMessage)
        .contains("access_token=eyJh****")
        .contains("session_id=abc1****")
        .contains("JSESSIONID=F123****")
        .contains("normal_cookie=normal_value");
  }

  @Test
  @DisplayName("Health check 요청은 필터링 제외")
  void shouldNotFilterHealthCheck() throws ServletException, IOException {
    // given
    request.setMethod("GET");
    request.setRequestURI("/api/v1/health");
    request.setCookies(new Cookie("test", "value"));

    // when
    boolean shouldFilter = !cookieLoggingFilter.shouldNotFilter(request);

    // then
    assertThat(shouldFilter).isFalse();
  }

  @Test
  @DisplayName("정적 리소스 요청은 필터링 제외")
  void shouldNotFilterStaticResources() throws ServletException, IOException {
    // given
    request.setMethod("GET");
    request.setRequestURI("/static/css/style.css");

    // when
    boolean shouldFilter = !cookieLoggingFilter.shouldNotFilter(request);

    // then
    assertThat(shouldFilter).isFalse();
  }

  @Test
  @DisplayName("API 요청은 필터링 대상")
  void shouldFilterApiRequests() throws ServletException, IOException {
    // given
    request.setMethod("POST");
    request.setRequestURI("/api/v1/users");

    // when
    boolean shouldFilter = !cookieLoggingFilter.shouldNotFilter(request);

    // then
    assertThat(shouldFilter).isTrue();
  }
}
