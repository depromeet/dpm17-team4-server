package depromeet.lessonfour.server.users.schemas.response;

import depromeet.lessonfour.server.users.domain.entities.User;
import depromeet.lessonfour.server.users.domain.values.Provider;

public record UserResponseDto(
    Long id,
    String email,
    String nickname,
    Provider provider,
    String profileImage,
    boolean isNew) {

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
