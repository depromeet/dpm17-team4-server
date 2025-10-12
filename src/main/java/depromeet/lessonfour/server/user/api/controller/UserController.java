package depromeet.lessonfour.server.user.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import depromeet.lessonfour.server.common.api.code.ErrorCode;
import depromeet.lessonfour.server.common.api.code.SuccessCode;
import depromeet.lessonfour.server.common.api.dto.SuccessResponse;
import depromeet.lessonfour.server.common.exception.ServerException;
import depromeet.lessonfour.server.user.app.dto.response.UserProfileResponseDto;
import depromeet.lessonfour.server.user.app.service.UserDeleteUseCase;
import depromeet.lessonfour.server.user.app.service.UserQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "사용자", description = "사용자 관련 API 문서입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

  private final UserQueryService userQueryService;
  private final UserDeleteUseCase userDeleteUseCase;

  @Operation(
      summary = "내 정보 조회",
      description = "현재 로그인한 사용자의 정보를 조회합니다.",
      security = {@SecurityRequirement(name = "JWT")})
  @GetMapping("/me")
  public ResponseEntity<SuccessResponse<UserProfileResponseDto>> getMe(
      @AuthenticationPrincipal(expression = "id") Long authenticatedUserId) {

    UserProfileResponseDto user =
        UserProfileResponseDto.of(userQueryService.getActivityUserById(authenticatedUserId));
    return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_FETCH, user));
  }

  @Operation(
      summary = "사용자 정보 조회",
      description = "지정된 ID의 사용자 정보를 조회합니다. 본인의 정보만 조회할 수 있습니다.",
      security = {@SecurityRequirement(name = "JWT")})
  @GetMapping("/{userId}")
  public ResponseEntity<SuccessResponse<UserProfileResponseDto>> getUser(
      @PathVariable Long userId,
      @AuthenticationPrincipal(expression = "id") Long authenticatedUserId) {

    // 요청한 사용자 ID와 인증된 사용자 ID가 일치하는지 확인
    if (!userId.equals(authenticatedUserId)) {
      throw new ServerException(ErrorCode.ACCESS_DENIED);
    }

    UserProfileResponseDto user =
        UserProfileResponseDto.of(userQueryService.getActivityUserById(userId));
    return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_FETCH, user));
  }

  @Operation(
      summary = "회원 탈퇴",
      description = "현재 로그인한 사용자의 계정을 삭제합니다.",
      security = {@SecurityRequirement(name = "JWT")})
  @DeleteMapping("/me")
  public SuccessResponse<Void> delete(@AuthenticationPrincipal(expression = "id") Long userId) {
    userDeleteUseCase.delete(userId);

    return SuccessResponse.of(SuccessCode.SUCCESS_DELETE);
  }
}
