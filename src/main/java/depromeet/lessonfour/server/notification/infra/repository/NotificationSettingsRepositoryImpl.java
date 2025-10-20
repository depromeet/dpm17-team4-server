package depromeet.lessonfour.server.notification.infra.repository;

import java.util.Optional;

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
  public Optional<NotificationSettings> findByRegistrationToken(String registrationToken) {
    return jpa.findByRegistrationToken(registrationToken);
  }

  @Override
  public boolean existsByRegistrationToken(String registrationToken) {
    return jpa.existsByRegistrationToken(registrationToken);
  }
}
