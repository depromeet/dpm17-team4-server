package depromeet.lessonfour.server.auth.api.dto.response;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import depromeet.lessonfour.server.auth.persist.jpa.entity.User;
import depromeet.lessonfour.server.auth.persist.jpa.entity.UserRoleEnum;
import depromeet.lessonfour.server.auth.value.Provider;

public record UserResponseDto(
    UUID id,
    String email,
    String nickname,
    UserRoleEnum role,
    Provider provider,
    @JsonProperty("profile_image") String profileImage,
    @JsonProperty("is_new") boolean isNew) {

  public static UserResponseDto of(User user) {
    return new UserResponseDto(
        user.getId(),
        user.getEmail(),
        user.getNickname(),
        user.getRole(),
        user.getProvider(),
        user.getProfileImage(),
        user.isNew());
  }
}
