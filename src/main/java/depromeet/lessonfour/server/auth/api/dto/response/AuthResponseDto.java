package depromeet.lessonfour.server.auth.api.dto.response;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import depromeet.lessonfour.server.auth.persist.jpa.entity.UserRoleEnum;
import depromeet.lessonfour.server.auth.value.Provider;

public record AuthResponseDto(
    UUID id,
    String email,
    String nickname,
    UserRoleEnum role,
    Provider provider,
    @JsonProperty("profile_image") String profileImage,
    @JsonProperty("is_new") boolean isNew,
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("refresh_token") String refreshToken) {

  public static AuthResponseDto of(UserResponseDto user, String accessToken, String refreshToken) {
    return new AuthResponseDto(
        user.id(),
        user.email(),
        user.nickname(),
        user.role(),
        user.provider(),
        user.profileImage(),
        user.isNew(),
        accessToken,
        refreshToken);
  }
}
