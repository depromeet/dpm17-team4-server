package depromeet.lessonfour.server.api;

import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Hidden;

@Hidden
@Profile("!prod")
@RestController
@RequestMapping("/api/v1/echo")
public class EchoController {

  // Echo JSON as-is
  @PostMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public String echoJson(@RequestBody String body) {
    return body;
  }

  // Echo plain text
  @PostMapping(
      path = "/text",
      consumes = MediaType.TEXT_PLAIN_VALUE,
      produces = MediaType.TEXT_PLAIN_VALUE)
  public String echoText(@RequestBody String body) {
    return body;
  }
}
