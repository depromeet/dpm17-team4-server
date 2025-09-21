package depromeet.lessonfour.server.user.app.dto.response;

import depromeet.lessonfour.server.user.domain.entity.User;
import depromeet.lessonfour.server.user.domain.vo.Provider;

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
