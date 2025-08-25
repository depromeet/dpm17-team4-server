package depromeet.lessonfour.server.auth.config.jwt.entrypoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;

public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException, ServletException {
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    response.setHeader("WWW-Authenticate", "Bearer realm=\"api\", error=\"invalid_token\"");

    LinkedHashMap<String, Object> body = getResponseBody(request);
    response.getWriter().write(objectMapper.writeValueAsString(body));
  }

  private LinkedHashMap<String, Object> getResponseBody(HttpServletRequest request) {
    LinkedHashMap<String, Object> body = new LinkedHashMap<>();
    body.put("status", HttpStatus.UNAUTHORIZED.value());
    body.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
    body.put("path", request.getRequestURI());
    body.put("timestamp", Instant.now().toString());
    return body;
  }
}
