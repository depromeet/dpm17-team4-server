package depromeet.lessonfour.server.notification.domain.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import depromeet.lessonfour.server.notification.domain.entity.NotificationSettings;

public interface NotificationSettingsRepository {

  NotificationSettings save(NotificationSettings notificationSettings);

  Optional<NotificationSettings> findById(Long settingsId);

  Optional<NotificationSettings> findByRegistrationToken(String registrationToken);

  boolean existsByRegistrationToken(String registrationToken);

  boolean existsByUserIdAndKey(Long userId, String key);

  Page<NotificationSettings> findByEnabled(Boolean enabled, Pageable pageable);
}
