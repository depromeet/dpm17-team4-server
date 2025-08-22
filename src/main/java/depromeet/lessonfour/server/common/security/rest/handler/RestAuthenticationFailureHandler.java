package depromeet.lessonfour.server.common.security.rest.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class RestAuthenticationFailureHandler implements AuthenticationFailureHandler {

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
      throws IOException {

    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    response.getWriter().write("{\"message\": \"Authentication failed\"}");

    String userAgent = request.getHeader("User-Agent");
    String attemptedEmail = request.getParameter("email");

    String safeId = attemptedEmail != null ? maskEmail(attemptedEmail) : "N/A";

    log.warn(
        "Authentication failed for user={}, agent={}, reason={}",
        safeId,
        userAgent,
        exception.getMessage());
  }

  // 이메일 마스킹 유틸
  private String maskEmail(String email) {
    int atIndex = email.indexOf("@");
    if (atIndex <= 1) return "****"; // 너무 짧은 경우
    String namePart = email.substring(0, atIndex);
    String domainPart = email.substring(atIndex);
    return namePart.charAt(0) + "****" + domainPart;
  }
}
