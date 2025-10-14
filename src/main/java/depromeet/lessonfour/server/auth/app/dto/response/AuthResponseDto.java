package depromeet.lessonfour.server.auth.app.dto.response;

import depromeet.lessonfour.server.auth.domain.vo.SocialProvider;
import depromeet.lessonfour.server.user.domain.entity.User;

public record AuthResponseDto(
    Long id,
    String email,
    String nickname,
    SocialProvider provider,
    String profileImage,
    boolean isNew,
    String accessToken,
    String refreshToken) {

  public static AuthResponseDto of(User user) {
    return new AuthResponseDto(
        user.getId(),
        user.getEmail(),
        user.getNickname(),
        SocialProvider.from(user.getProvider()),
        user.getProfileImage(),
        user.isNew(),
        null,
        null);
  }

  public static AuthResponseDto of(User user, TokenPairDto token) {
    return new AuthResponseDto(
        user.getId(),
        user.getEmail(),
        user.getNickname(),
        SocialProvider.from(user.getProvider()),
        user.getProfileImage(),
        user.isNew(),
        token.accessToken(),
        token.refreshToken());
  }
}
