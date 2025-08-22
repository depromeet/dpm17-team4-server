package depromeet.lessonfour.server.common.security.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
    @Email(message = "올바른 이메일 형식이 아닙니다") @NotBlank(message = "이메일은 필수입니다") String email,
    @NotBlank(message = "비밀번호는 필수입니다") String password) {}
