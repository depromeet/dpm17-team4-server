package depromeet.lessonfour.server.common.utils;

import static java.util.Optional.empty;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class HttpServletUtils {

  public static final Duration REFRESH_TOKEN_EXPIRATION = Duration.ofDays(7);
  public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

  private static final String BEARER_PREFIX = "Bearer ";
  private static final String AUTHORIZATION_HEADER = "Authorization";

  public Optional<String> getAccessToken(HttpServletRequest request) {
    if (request == null) {
      log.debug("HttpServletRequest is null");
      return empty();
    }

    Optional<String> authorization = Optional.ofNullable(request.getHeader(AUTHORIZATION_HEADER));

    return authorization
        .filter(auth -> auth.startsWith(BEARER_PREFIX))
        .map(auth -> auth.substring(BEARER_PREFIX.length()));
  }

  public Optional<String> getHeader(HttpServletRequest request, String name) {
    if (request == null || !StringUtils.hasText(name)) {
      return empty();
    }
    return Optional.ofNullable(request.getHeader(name));
  }

  public void putHeader(HttpServletResponse response, String name, String value) {
    if (response == null || !StringUtils.hasText(name) || value == null) {
      log.warn(
          "Invalid parameters for putHeader: response={}, name={}, value={}",
          response != null,
          name,
          value);
      return;
    }
    response.addHeader(name, value);
  }

  public void addCookie(
      HttpServletResponse response, String name, String value, Duration duration) {
    addCookie(response, name, value, (int) duration.getSeconds(), CookieOptions.secure());
  }

  public void addCookie(
      HttpServletResponse response, String name, String value, int seconds, CookieOptions options) {
    if (response == null || isInvalidCookieName(name) || value == null || seconds < 0) {
      log.warn(
          "Invalid parameters for addCookie: response={}, name={}, value={}, seconds={}",
          response != null,
          name,
          value != null,
          seconds);
      return;
    }

    try {
      setCookieHeader(response, name, value, seconds, options);
      log.debug(
          "Cookie added: name={}, maxAge={}, secure={}, sameSite={}",
          name,
          seconds,
          options.secure,
          options.sameSite);
    } catch (Exception e) {
      log.warn("Failed to add cookie: name={}, error={}", name, e.getMessage(), e);
    }
  }

  public void removeCookie(HttpServletRequest request, HttpServletResponse response, String name) {
    if (request == null || response == null || isInvalidCookieName(name)) {
      log.warn(
          "Invalid parameters for removeCookie: request={}, response={}, name={}",
          request != null,
          response != null,
          name);
      return;
    }

    if (getCookie(request, name).isPresent()) {
      try {
        setCookieHeader(response, name, "", 0, CookieOptions.secure());
        log.debug("Cookie removed: name={}", name);
      } catch (Exception e) {
        log.warn("Failed to remove cookie: name={}, error={}", name, e.getMessage(), e);
      }
    }
  }

  public Optional<Cookie> getCookie(HttpServletRequest request, String name) {
    if (request == null || !StringUtils.hasText(name)) {
      return empty();
    }

    Cookie[] cookies = request.getCookies();
    if (cookies != null && cookies.length > 0) {
      return Arrays.stream(cookies).filter(cookie -> name.equals(cookie.getName())).findFirst();
    }

    return empty();
  }

  private void setCookieHeader(
      HttpServletResponse response, String name, String value, int seconds, CookieOptions options) {
    String cookieHeader =
        String.format(
            "%s=%s; Path=%s; Max-Age=%d%s%s; SameSite=%s",
            name,
            value,
            options.path,
            seconds,
            options.httpOnly ? "; HttpOnly" : "",
            options.secure ? "; Secure" : "",
            options.sameSite.getValue());

    response.addHeader("Set-Cookie", cookieHeader);
  }

  private boolean isInvalidCookieName(String name) {
    if (!StringUtils.hasText(name)) {
      return true;
    }

    return !name.matches("^[a-zA-Z0-9_-]+$") || name.length() > 256;
  }

  public static class CookieOptions {
    private final String path;
    private final boolean httpOnly;
    private final boolean secure;
    private final SameSite sameSite;

    private CookieOptions(String path, boolean httpOnly, boolean secure, SameSite sameSite) {
      this.path = path;
      this.httpOnly = httpOnly;
      this.secure = secure;
      this.sameSite = sameSite;
    }

    // 보안 쿠키 옵션 (HTTPS 환경용)
    public static CookieOptions secure() {
      return new CookieOptions("/", true, true, SameSite.NONE);
    }

    // 개발 환경용 쿠키 옵션 (HTTP 허용)
    public static CookieOptions development() {
      return new CookieOptions("/", true, false, SameSite.LAX);
    }

    public static CookieOptions custom(
        String path, boolean httpOnly, boolean secure, SameSite sameSite) {
      return new CookieOptions(path, httpOnly, secure, sameSite);
    }
  }

  public enum SameSite {
    STRICT("Strict"),
    LAX("Lax"),
    NONE("None");

    private final String value;

    SameSite(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }
}
