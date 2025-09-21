package depromeet.lessonfour.server.auth.domain.vo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import depromeet.lessonfour.server.user.domain.entity.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountContext implements UserDetails {

  private Long id;
  private String email;
  private String password;
  private String nickname;

  public static AccountContext of(User user) {
    return AccountContext.builder()
        .id(user.getId())
        .email(user.getEmail())
        .password(user.getPassword())
        .nickname(user.getNickname())
        .build();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    List<GrantedAuthority> authorities = new ArrayList<>();
    // 모든 사용자에게 기본 ROLE_USER 부여
    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
    return authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return email;
  }
}
