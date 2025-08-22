package depromeet.lessonfour.server.auth.api;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import depromeet.lessonfour.server.auth.service.UserQueryService;
import lombok.RequiredArgsConstructor;

@Profile("dev")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthTestController {

  private final UserQueryService userQueryService;

  @GetMapping("/{email}")
  public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
    return ResponseEntity.ok(userQueryService.findByEmail(email));
  }
}
