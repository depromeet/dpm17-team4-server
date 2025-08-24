package depromeet.lessonfour.server.common.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HttpServletUtilsTest {

  private HttpServletUtils httpServletUtils;
  private HttpServletRequest request;
  private HttpServletResponse response;

  @BeforeEach
  void setUp() {
    httpServletUtils = new HttpServletUtils();
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
  }

  @Nested
  @DisplayName("getAccessToken 테스트")
  class GetAccessTokenTest {

    @Test
    @DisplayName("유효한 Bearer 토큰이 있을 때 토큰을 반환한다")
    void whenValidBearerToken_thenReturnToken() {
      // given
      when(request.getHeader("Authorization")).thenReturn("Bearer valid-token-123");

      // when
      Optional<String> result = httpServletUtils.getAccessToken(request);

      // then
      assertThat(result).isPresent();
      assertThat(result.get()).isEqualTo("valid-token-123");
    }

    @Test
    @DisplayName("Bearer 접두사가 없을 때 빈 Optional을 반환한다")
    void whenNoBearerPrefix_thenReturnEmpty() {
      // given
      when(request.getHeader("Authorization")).thenReturn("Basic invalid-token");

      // when
      Optional<String> result = httpServletUtils.getAccessToken(request);

      // then
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Authorization 헤더가 없을 때 빈 Optional을 반환한다")
    void whenNoAuthorizationHeader_thenReturnEmpty() {
      // given
      when(request.getHeader("Authorization")).thenReturn(null);

      // when
      Optional<String> result = httpServletUtils.getAccessToken(request);

      // then
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("request가 null일 때 빈 Optional을 반환한다")
    void whenRequestIsNull_thenReturnEmpty() {
      // when
      Optional<String> result = httpServletUtils.getAccessToken(null);

      // then
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("addCookie 테스트")
  class AddCookieTest {

    @Test
    @DisplayName("기본 보안 쿠키를 올바르게 생성한다")
    void whenAddSecureCookie_thenCreateCorrectCookie() {
      // given
      String cookieName = "sessionId";
      String cookieValue = "test-session-123";
      int maxAge = 3600;

      // when
      httpServletUtils.addCookie(response, cookieName, cookieValue, maxAge);

      // then
      verify(response)
          .addHeader(eq("Set-Cookie"), ArgumentMatchers.contains("sessionId=test-session-123"));
      verify(response).addHeader(eq("Set-Cookie"), ArgumentMatchers.contains("HttpOnly"));
      verify(response).addHeader(eq("Set-Cookie"), ArgumentMatchers.contains("Secure"));
      verify(response).addHeader(eq("Set-Cookie"), ArgumentMatchers.contains("SameSite=None"));
    }

    @Test
    @DisplayName("개발 환경용 쿠키를 올바르게 생성한다")
    void whenAddDevelopmentCookie_thenCreateCorrectCookie() {
      // given
      String cookieName = "devSession";
      String cookieValue = "dev-value";
      int maxAge = 1800;
      HttpServletUtils.CookieOptions options = HttpServletUtils.CookieOptions.development();

      // when
      httpServletUtils.addCookie(response, cookieName, cookieValue, maxAge, options);

      // then
      verify(response)
          .addHeader(eq("Set-Cookie"), ArgumentMatchers.contains("devSession=dev-value"));
      verify(response).addHeader(eq("Set-Cookie"), ArgumentMatchers.contains("HttpOnly"));
      verify(response, never()).addHeader(eq("Set-Cookie"), ArgumentMatchers.contains("Secure"));
      verify(response).addHeader(eq("Set-Cookie"), ArgumentMatchers.contains("SameSite=Lax"));
    }

    @Test
    @DisplayName("잘못된 쿠키 이름일 때 쿠키를 생성하지 않는다")
    void whenInvalidCookieName_thenDoNotCreateCookie() {
      // given
      String invalidName = "invalid name with spaces";
      String cookieValue = "value";
      int maxAge = 3600;

      // when
      httpServletUtils.addCookie(response, invalidName, cookieValue, maxAge);

      // then
      verify(response, never()).addHeader(eq("Set-Cookie"), any(String.class));
    }

    @Test
    @DisplayName("null 파라미터가 있을 때 쿠키를 생성하지 않는다")
    void whenNullParameters_thenDoNotCreateCookie() {
      // when & then
      httpServletUtils.addCookie(null, "name", "value", 3600);
      httpServletUtils.addCookie(response, null, "value", 3600);
      httpServletUtils.addCookie(response, "name", null, 3600);
      httpServletUtils.addCookie(response, "name", "value", -1);

      verify(response, never()).addHeader(eq("Set-Cookie"), any(String.class));
    }
  }

  @Nested
  @DisplayName("removeCookie 테스트")
  class RemoveCookieTest {

    @Test
    @DisplayName("존재하는 쿠키를 올바르게 제거한다")
    void whenCookieExists_thenRemoveCookie() {
      // given
      String cookieName = "sessionId";
      Cookie existingCookie = new Cookie(cookieName, "some-value");
      when(request.getCookies()).thenReturn(new Cookie[] {existingCookie});

      // when
      httpServletUtils.removeCookie(request, response, cookieName);

      // then
      verify(response).addHeader(eq("Set-Cookie"), ArgumentMatchers.contains("sessionId=;"));
      verify(response).addHeader(eq("Set-Cookie"), ArgumentMatchers.contains("Max-Age=0"));
      verify(response).addHeader(eq("Set-Cookie"), ArgumentMatchers.contains("HttpOnly"));
      verify(response).addHeader(eq("Set-Cookie"), ArgumentMatchers.contains("Secure"));
    }

    @Test
    @DisplayName("존재하지 않는 쿠키는 제거하지 않는다")
    void whenCookieDoesNotExist_thenDoNotRemove() {
      // given
      when(request.getCookies()).thenReturn(new Cookie[] {});

      // when
      httpServletUtils.removeCookie(request, response, "nonExistentCookie");

      // then
      verify(response, never()).addHeader(eq("Set-Cookie"), any(String.class));
    }
  }

  @Nested
  @DisplayName("getCookie 테스트")
  class GetCookieTest {

    @Test
    @DisplayName("존재하는 쿠키를 올바르게 가져온다")
    void whenCookieExists_thenReturnCookie() {
      // given
      String cookieName = "testCookie";
      Cookie expectedCookie = new Cookie(cookieName, "testValue");
      Cookie[] cookies = {new Cookie("other", "value"), expectedCookie};
      when(request.getCookies()).thenReturn(cookies);

      // when
      Optional<Cookie> result = httpServletUtils.getCookie(request, cookieName);

      // then
      assertThat(result).isPresent();
      assertThat(result.get().getName()).isEqualTo(cookieName);
      assertThat(result.get().getValue()).isEqualTo("testValue");
    }

    @Test
    @DisplayName("존재하지 않는 쿠키는 빈 Optional을 반환한다")
    void whenCookieDoesNotExist_thenReturnEmpty() {
      // given
      Cookie[] cookies = {new Cookie("other", "value")};
      when(request.getCookies()).thenReturn(cookies);

      // when
      Optional<Cookie> result = httpServletUtils.getCookie(request, "nonExistent");

      // then
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("쿠키가 없을 때 빈 Optional을 반환한다")
    void whenNoCookies_thenReturnEmpty() {
      // given
      when(request.getCookies()).thenReturn(null);

      // when
      Optional<Cookie> result = httpServletUtils.getCookie(request, "anyCookie");

      // then
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("getHeader 테스트")
  class GetHeaderTest {

    @Test
    @DisplayName("존재하는 헤더를 올바르게 가져온다")
    void whenHeaderExists_thenReturnHeader() {
      // given
      when(request.getHeader("Content-Type")).thenReturn("application/json");

      // when
      Optional<String> result = httpServletUtils.getHeader(request, "Content-Type");

      // then
      assertThat(result).isPresent();
      assertThat(result.get()).isEqualTo("application/json");
    }

    @Test
    @DisplayName("존재하지 않는 헤더는 빈 Optional을 반환한다")
    void whenHeaderDoesNotExist_thenReturnEmpty() {
      // given
      when(request.getHeader("Non-Existent")).thenReturn(null);

      // when
      Optional<String> result = httpServletUtils.getHeader(request, "Non-Existent");

      // then
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("putHeader 테스트")
  class PutHeaderTest {

    @Test
    @DisplayName("유효한 헤더를 올바르게 추가한다")
    void whenValidHeader_thenAddHeader() {
      // when
      httpServletUtils.putHeader(response, "X-Custom-Header", "custom-value");

      // then
      verify(response).addHeader("X-Custom-Header", "custom-value");
    }

    @Test
    @DisplayName("잘못된 파라미터일 때 헤더를 추가하지 않는다")
    void whenInvalidParameters_thenDoNotAddHeader() {
      // when & then
      httpServletUtils.putHeader(null, "name", "value");
      httpServletUtils.putHeader(response, null, "value");
      httpServletUtils.putHeader(response, "", "value");
      httpServletUtils.putHeader(response, "name", null);

      verify(response, never()).addHeader(any(), any());
    }
  }
}
