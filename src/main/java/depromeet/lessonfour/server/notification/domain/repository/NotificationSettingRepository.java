package depromeet.lessonfour.server.notification.domain.repository;

import java.util.Optional;

import depromeet.lessonfour.server.notification.domain.entity.NotificationSetting;

public interface NotificationSettingRepository {

  NotificationSetting save(NotificationSetting notificationSetting);

  Optional<NotificationSetting> findByRegistrationToken(String registrationToken);

  boolean existsByRegistrationToken(String registrationToken);
}
