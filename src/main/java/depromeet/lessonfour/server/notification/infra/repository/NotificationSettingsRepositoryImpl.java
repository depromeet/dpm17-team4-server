package depromeet.lessonfour.server.notification.infra.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.notification.domain.entity.NotificationSettings;
import depromeet.lessonfour.server.notification.domain.repository.NotificationSettingsRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NotificationSettingsRepositoryImpl implements NotificationSettingsRepository {

  private final JpaNotificationSettingsRepository jpa;

  @Transactional
  @Override
  public NotificationSettings save(NotificationSettings notificationSettings) {
    return jpa.save(notificationSettings);
  }

  @Override
  public Optional<NotificationSettings> findById(Long settingsId) {
    return jpa.findById(settingsId);
  }

  @Override
  public List<NotificationSettings> findAllByUserId(Long userId) {
    return jpa.findAllByUserId(userId);
  }

  @Override
  public Optional<NotificationSettings> findByRegistrationToken(String registrationToken) {
    return jpa.findByRegistrationToken(registrationToken);
  }

  @Override
  public boolean existsByRegistrationToken(String registrationToken) {
    return jpa.existsByRegistrationToken(registrationToken);
  }

  @Override
  public boolean existsByUserIdAndKey(Long userId, String key) {
    return jpa.existsByUserIdAndKey(userId, key);
  }

  @Transactional
  @Override
  public void delete(NotificationSettings notificationSettings) {
    jpa.delete(notificationSettings);
  }

  @Override
  public Page<NotificationSettings> findByEnabled(Boolean enabled, Pageable pageable) {
    return jpa.findByEnabled(enabled, pageable);
  }
}
