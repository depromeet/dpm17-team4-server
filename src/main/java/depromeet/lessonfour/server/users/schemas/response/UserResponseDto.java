package depromeet.lessonfour.server.users.schemas.response;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import depromeet.lessonfour.server.users.domain.entities.User;
import depromeet.lessonfour.server.users.domain.values.Provider;

public record UserResponseDto(
    UUID id,
    String email,
    String nickname,
    Provider provider,
    @JsonProperty("profile_image") String profileImage,
    @JsonProperty("is_new") boolean isNew) {

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
