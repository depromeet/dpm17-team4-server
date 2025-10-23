package depromeet.lessonfour.server.user.app.dto.response;

import depromeet.lessonfour.server.user.domain.entity.User;
import depromeet.lessonfour.server.user.domain.vo.Gender;

public record UpdateUserProfileResponseDto(
    Long id,
    String email,
    String nickname,
    String profileImage,
    Gender gender,
    Integer birthYear
) {
    public static UpdateUserProfileResponseDto of(User user) {
        return new UpdateUserProfileResponseDto(
            user.getId(),
            user.getEmail(),
            user.getNickname(),
            user.getProfileImage(),
            user.getGender(),
            user.getBirthYear()
        );
    }
}
