package depromeet.lessonfour.server.notification.infra.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import depromeet.lessonfour.server.notification.domain.entity.NotificationSettings;

public interface JpaNotificationSettingsRepository
    extends JpaRepository<NotificationSettings, Long> {

  Optional<NotificationSettings> findByRegistrationToken(String registrationToken);

  boolean existsByRegistrationToken(String registrationToken);
}
