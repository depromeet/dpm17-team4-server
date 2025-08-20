package depromeet.lessonfour.server.api;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import depromeet.lessonfour.server.user.User;
import depromeet.lessonfour.server.user.UserRepository;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserRepository userRepository;

  public UserController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @PostMapping
  public ResponseEntity<User> create(@RequestBody User user) {
    User saved = userRepository.save(user);
    return ResponseEntity.ok(saved);
  }

  @GetMapping
  public List<User> list() {
    return userRepository.findAll();
  }

  @GetMapping("/{id}")
  public ResponseEntity<User> get(@PathVariable Long id) {
    Optional<User> user = userRepository.findById(id);
    return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PatchMapping("/{id}")
  public ResponseEntity<User> update(@PathVariable Long id, @RequestBody User payload) {
    return userRepository
        .findById(id)
        .map(
            u -> {
              if (payload.getEmail() != null) u.setEmail(payload.getEmail());
              if (payload.getUsername() != null) u.setUsername(payload.getUsername());
              return ResponseEntity.ok(userRepository.save(u));
            })
        .orElseGet(() -> ResponseEntity.notFound().build());
  }
}
