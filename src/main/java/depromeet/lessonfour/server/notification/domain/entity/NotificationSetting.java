package depromeet.lessonfour.server.notification.domain.entity;

import depromeet.lessonfour.server.common.domain.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification_settings")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSetting extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull @Column(nullable = false)
  private Long userId;

  @NotNull @Column(unique = true, nullable = false, length = 512)
  private String registrationToken;

  @NotNull @Column(nullable = false)
  private Boolean enabled;

  public static NotificationSetting create(Long userId, String registrationToken, Boolean enabled) {
    return NotificationSetting.builder()
        .userId(userId)
        .registrationToken(registrationToken)
        .enabled(enabled)
        .build();
  }

  public void updateEnabled(Boolean enabled) {
    this.enabled = enabled;
  }
}
