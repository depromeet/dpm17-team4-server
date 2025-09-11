package depromeet.lessonfour.server.auth.persist.jpa.entity;

import java.util.UUID;

import depromeet.lessonfour.server.auth.value.Provider;
import depromeet.lessonfour.server.common.persist.jpa.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotNull @Column(unique = true, nullable = false)
  private String email;

  @Column private String password;

  @NotNull @Column(unique = true, nullable = false)
  private String nickname;

  @Embedded private Provider provider;

  @Column(length = 500)
  private String profileImage;

  @Column(length = 512)
  private String refreshToken;

  @Transient @Builder.Default private boolean isNew = false;

  public static User register(
      String email, String nickname, String password, String profileImage, Provider provider) {
    return User.builder()
        .email(email)
        .nickname(nickname)
        .password(password)
        .profileImage(profileImage)
        .provider(provider)
        .isNew(true)
        .build();
  }

  public static User register(String email, String nickname, String password) {
    return register(email, nickname, password, null, null);
  }

  public static User register(String email, String nickname, String password, String profileImage) {
    return register(email, nickname, password, profileImage, null);
  }

  public void storeRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public boolean isNew() {
    return isNew;
  }
}
