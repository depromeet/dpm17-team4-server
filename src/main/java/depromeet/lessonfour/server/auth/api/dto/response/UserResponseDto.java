package depromeet.lessonfour.server.auth.api.dto.response;

import java.util.UUID;

import depromeet.lessonfour.server.auth.persist.jpa.entity.LoginProvider;
import depromeet.lessonfour.server.auth.persist.jpa.entity.UserRoleEnum;

public record UserResponseDto(
    UUID id,
    String email,
    String password,
    String nickname,
    UserRoleEnum role,
    LoginProvider provider,
    String providerUserId) {}
