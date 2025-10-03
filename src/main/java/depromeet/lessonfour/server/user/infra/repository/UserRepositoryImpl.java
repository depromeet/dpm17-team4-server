package depromeet.lessonfour.server.user.infra.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.user.domain.User;
import depromeet.lessonfour.server.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

  private final JpaUserRepository jpa;

  @Override
  public Optional<User> findActiveById(Long id) {
    return jpa.findByIdAndIsDeletedFalse(id);
  }

  @Override
  public Optional<User> findById(Long id) {
    return jpa.findById(id);
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return jpa.findByEmail(email);
  }

  @Override
  public boolean existsActiveByEmail(String email) {
    return jpa.existsByEmailAndIsDeletedFalse(email);
  }

  @Transactional
  @Override
  public User save(User user) {
    return jpa.save(user);
  }
}
