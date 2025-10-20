package depromeet.lessonfour.server.notification.infra.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import depromeet.lessonfour.server.notification.domain.entity.NotificationSetting;

public interface JpaNotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {

  Optional<NotificationSetting> findByRegistrationToken(String registrationToken);

  boolean existsByRegistrationToken(String registrationToken);
}
