package depromeet.lessonfour.server.auth.persist.jpa.entity;

import java.util.UUID;

import depromeet.lessonfour.server.common.persist.jpa.entity.BaseTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

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

  @Enumerated(EnumType.STRING)
  private UserRoleEnum role;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private LoginProvider provider;

  private String providerUserId;

  @Column private String refreshToken;

  public static User register(String email, String nickname, String password) {
    return User.builder()
        .email(email)
        .nickname(nickname)
        .password(password)
        .role(UserRoleEnum.USER)
        .provider(LoginProvider.LOCAL)
        .build();
  }

  public void storeRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public String getAuthority() {
    return role.getAuthority();
  }
}
