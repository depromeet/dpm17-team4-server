package depromeet.lessonfour.server.user.app.dto.response;

import depromeet.lessonfour.server.user.domain.entity.User;

public record UserProfileResponseDto(Long id, String email, String nickname, String profileImage) {

  public static UserProfileResponseDto of(User user) {
    return new UserProfileResponseDto(
        user.getId(), user.getEmail(), user.getNickname(), user.getProfileImage());
  }
}
