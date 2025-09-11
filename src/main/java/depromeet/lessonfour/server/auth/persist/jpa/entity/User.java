package depromeet.lessonfour.server.auth.persist.jpa.entity;

import java.util.UUID;

import depromeet.lessonfour.server.auth.value.Provider;
import depromeet.lessonfour.server.auth.value.Provider.ProviderType;
import depromeet.lessonfour.server.common.persist.jpa.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

  @Column
  private String password;

  @NotNull @Column(unique = true, nullable = false)
  private String nickname;

  @Enumerated(EnumType.STRING)
  private UserRoleEnum role;

  @Embedded
  private Provider provider;

  @Column(length = 500)
  private String profileImage;

  @Column(length = 512)
  private String refreshToken;

  public static User register(String email, String nickname, String password) {
    return User.builder()
        .email(email)
        .nickname(nickname)
        .password(password)
        .role(UserRoleEnum.USER)
        .provider(Provider.of(ProviderType.LOCAL, null))
        .build();
  }

  public void storeRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public String getAuthority() {
    return role.getAuthority();
  }
}
