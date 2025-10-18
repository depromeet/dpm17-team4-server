package depromeet.lessonfour.server.auth.app.dto.response;

import depromeet.lessonfour.server.user.domain.entity.User;
import depromeet.lessonfour.server.user.domain.vo.Provider;

public record AuthResponseDto(
    Long id,
    String email,
    String nickname,
    Provider.ProviderType provider,
    String profileImage,
    boolean isNew,
    String accessToken,
    String refreshToken) {

  public static AuthResponseDto of(User user) {
    return new AuthResponseDto(
        user.getId(),
        user.getEmail(),
        user.getNickname(),
        user.getProvider() != null ? user.getProvider().getType() : null,
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
        user.getProvider() != null ? user.getProvider().getType() : null,
        user.getProfileImage(),
        user.isNew(),
        token.accessToken(),
        token.refreshToken());
  }
}
