package depromeet.lessonfour.server.user.infra.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import depromeet.lessonfour.server.user.domain.User;

public interface JpaUserRepository extends JpaRepository<User, Long> {

  Optional<User> findByIdAndIsDeletedFalse(Long id);

  Optional<User> findByEmail(String email);

  boolean existsByEmailAndIsDeletedFalse(String email);
}
