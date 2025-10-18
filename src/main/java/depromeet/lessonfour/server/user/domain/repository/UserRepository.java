package depromeet.lessonfour.server.user.domain.repository;

import java.util.Optional;

import depromeet.lessonfour.server.user.domain.entity.User;

public interface UserRepository {

  Optional<User> findActiveById(Long id);

  Optional<User> findById(Long id);

  Optional<User> findByEmail(String email);

  boolean existsActiveByEmail(String email);

  User save(User user);
}
