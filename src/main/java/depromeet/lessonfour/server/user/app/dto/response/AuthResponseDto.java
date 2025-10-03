package depromeet.lessonfour.server.user.app.dto.response;

import depromeet.lessonfour.server.user.domain.Provider;

public record AuthResponseDto(
    Long id,
    String email,
    String nickname,
    Provider provider,
    String profileImage,
    boolean isNew,
    String accessToken,
    String refreshToken) {

  public static AuthResponseDto of(UserResponseDto user, String accessToken, String refreshToken) {
    return new AuthResponseDto(
        user.id(),
        user.email(),
        user.nickname(),
        user.provider(),
        user.profileImage(),
        user.isNew(),
        accessToken,
        refreshToken);
  }
}
