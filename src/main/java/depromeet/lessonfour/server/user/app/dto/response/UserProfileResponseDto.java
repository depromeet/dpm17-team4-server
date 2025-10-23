package depromeet.lessonfour.server.user.app.dto.response;

import depromeet.lessonfour.server.user.domain.entity.User;
import depromeet.lessonfour.server.user.domain.vo.Gender;
import depromeet.lessonfour.server.user.domain.vo.Provider;

public record UserProfileResponseDto(
    Long id,
    String email,
    String nickname,
    String profileImage,
    Provider provider,
    Gender gender,
    Integer birthYear) {

  public static UserProfileResponseDto of(User user) {
    return new UserProfileResponseDto(
        user.getId(),
        user.getEmail(),
        user.getNickname(),
        user.getProfileImage(),
        user.getProvider(),
        user.getGender(),
        user.getBirthYear());
  }
}
