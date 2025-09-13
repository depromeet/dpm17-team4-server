package depromeet.lessonfour.server.users.domain.ports;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import depromeet.lessonfour.server.users.domain.entities.User;

public interface UserRepository extends JpaRepository<User, UUID> {

  Optional<User> findByEmail(String email);

  Optional<User> findByNickname(String nickname);

  boolean existsByEmail(String email);

  boolean existsByNickname(String nickname);
}
