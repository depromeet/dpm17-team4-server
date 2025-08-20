package depromeet.lessonfour.server.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hello")
@RequiredArgsConstructor
public class HelloController {

  @GetMapping
  public String hello() {
    return "Hello, World!";
  }
}
