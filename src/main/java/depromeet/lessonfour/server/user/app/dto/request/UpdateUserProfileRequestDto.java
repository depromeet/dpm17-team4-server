package depromeet.lessonfour.server.user.app.dto.request;

import depromeet.lessonfour.server.user.app.validator.ValidBirthYear;
import depromeet.lessonfour.server.user.domain.vo.Gender;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UpdateUserProfileRequestDto(
    @Size(max = 32, message = "닉네임은 32자를 초과할 수 없습니다") String nickname,
    @Size(max = 512, message = "프로필 이미지 URL은 512자를 초과할 수 없습니다") String profileImage,
    Gender gender,
    @ValidBirthYear Integer birthYear) {}
