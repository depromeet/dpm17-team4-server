package depromeet.lessonfour.server.auth.api.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import depromeet.lessonfour.server.user.app.service.UserQueryService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;

@Hidden
@Profile("dev")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthTestController {

  private final UserQueryService userQueryService;

  @GetMapping("/{email}")
  public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
    return ResponseEntity.ok(userQueryService.getUserByEmail(email));
  }
}
