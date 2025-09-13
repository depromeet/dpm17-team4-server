package depromeet.lessonfour.server.users.controllers;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import depromeet.lessonfour.server.users.services.UserQueryService;
import lombok.RequiredArgsConstructor;

@Profile("dev")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthTestController {

  private final UserQueryService userQueryService;

  @GetMapping("/{email}")
  public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
    return ResponseEntity.ok(userQueryService.findByEmail(email));
  }
}
