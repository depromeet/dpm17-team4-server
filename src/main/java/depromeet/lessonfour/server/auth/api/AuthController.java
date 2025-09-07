package depromeet.lessonfour.server.auth.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import depromeet.lessonfour.server.auth.api.dto.request.RegisterRequestDto;
import depromeet.lessonfour.server.auth.service.RegisterUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

  private final RegisterUseCase registerUseCase;

  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDto dto) {
    registerUseCase.register(dto);
    return ResponseEntity.ok().build();
  }
}
