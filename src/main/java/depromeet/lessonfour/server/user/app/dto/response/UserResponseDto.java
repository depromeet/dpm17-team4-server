package depromeet.lessonfour.server.user.app.dto.response;

import depromeet.lessonfour.server.user.domain.Provider;
import depromeet.lessonfour.server.user.domain.User;

public record UserResponseDto(
    Long id, String email, String nickname, Provider provider, String profileImage, boolean isNew) {

  public static UserResponseDto of(User user) {
    return new UserResponseDto(
        user.getId(),
        user.getEmail(),
        user.getNickname(),
        user.getProvider(),
        user.getProfileImage(),
        user.isNew());
  }
}
