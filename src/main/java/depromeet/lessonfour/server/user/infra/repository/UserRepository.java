package depromeet.lessonfour.server.user.infra.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import depromeet.lessonfour.server.user.domain.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);

  Optional<User> findByNickname(String nickname);

  boolean existsByEmail(String email);

  boolean existsByNickname(String nickname);
}
