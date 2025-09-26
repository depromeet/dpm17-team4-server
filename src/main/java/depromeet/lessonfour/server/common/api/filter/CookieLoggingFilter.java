package depromeet.lessonfour.server.common.api.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CookieLoggingFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    logRequestCookies(request);
    filterChain.doFilter(request, response);
  }

  private void logRequestCookies(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();

    if (cookies == null || cookies.length == 0) {
      log.debug(
          "[CookieLog] Request: {} {} - No cookies present",
          request.getMethod(),
          request.getRequestURI());
      return;
    }

    String cookieInfo =
        Arrays.stream(cookies)
            .map(cookie -> String.format("%s=%s", cookie.getName(), maskSensitiveCookie(cookie)))
            .collect(Collectors.joining(", "));

    log.info(
        "[CookieLog] Request: {} {} - Cookies: [{}]",
        request.getMethod(),
        request.getRequestURI(),
        cookieInfo);
  }

  private String maskSensitiveCookie(Cookie cookie) {
    String cookieName = cookie.getName().toLowerCase();
    String cookieValue = cookie.getValue();

    // JWT 토큰이나 세션 관련 쿠키는 마스킹 처리
    if (cookieName.contains("token")
        || cookieName.contains("session")
        || cookieName.contains("auth")
        || cookieName.equals("jsessionid")) {

      if (cookieValue.length() <= 4) {
        return "****";
      }
      return cookieValue.substring(0, 4) + "****";
    }

    return cookieValue;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String requestURI = request.getRequestURI();

    // Health check이나 정적 리소스 요청은 로깅 제외
    return requestURI.startsWith("/actuator/")
        || requestURI.startsWith("/static/")
        || requestURI.startsWith("/public/")
        || requestURI.equals("/api/v1/health");
  }
}
