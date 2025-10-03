package depromeet.lessonfour.server.user.domain;

import java.util.Optional;

public interface UserRepository {

  Optional<User> findActiveById(Long id);

  Optional<User> findById(Long id);

  Optional<User> findByEmail(String email);

  boolean existsActiveByEmail(String email);

  User save(User user);
}
