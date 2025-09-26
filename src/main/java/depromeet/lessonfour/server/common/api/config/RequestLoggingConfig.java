package depromeet.lessonfour.server.common.api.config;

import java.io.IOException;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import depromeet.lessonfour.server.common.api.filter.CookieLoggingFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RequestLoggingConfig {

  private final CookieLoggingFilter cookieLoggingFilter;

  @Bean
  @Profile({"local", "dev"})
  public CommonsRequestLoggingFilter requestLoggingFilter() {
    CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
    filter.setIncludeClientInfo(true);
    filter.setIncludeQueryString(true);
    filter.setIncludePayload(true);
    filter.setMaxPayloadLength(1000);
    filter.setIncludeHeaders(false);
    return filter;
  }

  @Bean
  @Profile({"local", "dev"})
  public FilterRegistrationBean<CookieLoggingFilter> cookieLoggingFilterRegistration() {
    FilterRegistrationBean<CookieLoggingFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(cookieLoggingFilter);
    registration.addUrlPatterns("/api/*");
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
    registration.setName("cookieLoggingFilter");
    return registration;
  }

  @Bean
  @Profile({"local", "dev"})
  public FilterRegistrationBean<OncePerRequestFilter> contentCachingRequestFilter() {
    OncePerRequestFilter wrapperFilter =
        new OncePerRequestFilter() {
          @Override
          protected boolean shouldNotFilter(HttpServletRequest request) {
            String ct = request.getContentType();
            if (ct == null) return false;
            String lower = ct.toLowerCase();
            return lower.startsWith("multipart/") || lower.startsWith("application/octet-stream");
          }

          @Override
          protected void doFilterInternal(
              HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
              throws ServletException, IOException {
            ContentCachingRequestWrapper wrapped = new ContentCachingRequestWrapper(request);
            filterChain.doFilter(wrapped, response);
          }
        };
    FilterRegistrationBean<OncePerRequestFilter> reg = new FilterRegistrationBean<>(wrapperFilter);
    reg.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return reg;
  }
}
