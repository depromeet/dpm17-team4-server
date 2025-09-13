package depromeet.lessonfour.server.users.schemas.response;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import depromeet.lessonfour.server.users.domain.values.Provider;

public record AuthResponseDto(
    UUID id,
    String email,
    String nickname,
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
        user.provider(),
        user.profileImage(),
        user.isNew(),
        accessToken,
        refreshToken);
  }
}
