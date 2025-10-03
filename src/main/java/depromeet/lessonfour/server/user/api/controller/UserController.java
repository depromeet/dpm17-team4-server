package depromeet.lessonfour.server.user.api.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import depromeet.lessonfour.server.common.api.code.SuccessCode;
import depromeet.lessonfour.server.common.api.dto.SuccessResponse;
import depromeet.lessonfour.server.user.app.service.UserDeleteUseCase;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

  private final UserDeleteUseCase userDeleteUseCase;

  @DeleteMapping("/me")
  public SuccessResponse<Void> delete(@AuthenticationPrincipal(expression = "id") Long userId) {
    userDeleteUseCase.delete(userId);

    return SuccessResponse.of(SuccessCode.SUCCESS_DELETE);
  }
}
