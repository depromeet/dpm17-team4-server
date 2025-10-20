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
public class NotificationSettings extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull @Column(nullable = false)
  private Long userId;

  @NotNull @Column(nullable = false, length = 32)
  private String key;

  @NotNull @Column(unique = true, nullable = false, length = 512)
  private String registrationToken;

  @NotNull @Column(nullable = false)
  private Boolean enabled;

  public static NotificationSettings create(
      Long userId, String key, String registrationToken, Boolean enabled) {
    return NotificationSettings.builder()
        .userId(userId)
        .key(key)
        .registrationToken(registrationToken)
        .enabled(enabled)
        .build();
  }

  public void updateKey(String key) {
    this.key = key;
  }

  public void updateEnabled(Boolean enabled) {
    this.enabled = enabled;
  }
}
