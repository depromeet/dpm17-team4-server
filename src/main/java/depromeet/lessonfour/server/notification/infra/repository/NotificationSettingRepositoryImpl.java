package depromeet.lessonfour.server.notification.infra.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.notification.domain.entity.NotificationSetting;
import depromeet.lessonfour.server.notification.domain.repository.NotificationSettingRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NotificationSettingRepositoryImpl implements NotificationSettingRepository {

  private final JpaNotificationSettingRepository jpa;

  @Transactional
  @Override
  public NotificationSetting save(NotificationSetting notificationSetting) {
    return jpa.save(notificationSetting);
  }

  @Override
  public Optional<NotificationSetting> findByRegistrationToken(String registrationToken) {
    return jpa.findByRegistrationToken(registrationToken);
  }

  @Override
  public boolean existsByRegistrationToken(String registrationToken) {
    return jpa.existsByRegistrationToken(registrationToken);
  }
}
