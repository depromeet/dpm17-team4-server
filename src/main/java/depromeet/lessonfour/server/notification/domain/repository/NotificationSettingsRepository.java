package depromeet.lessonfour.server.notification.domain.repository;

import java.util.Optional;

import depromeet.lessonfour.server.notification.domain.entity.NotificationSettings;

public interface NotificationSettingsRepository {

  NotificationSettings save(NotificationSettings notificationSettings);

  Optional<NotificationSettings> findByRegistrationToken(String registrationToken);

  boolean existsByRegistrationToken(String registrationToken);
}
