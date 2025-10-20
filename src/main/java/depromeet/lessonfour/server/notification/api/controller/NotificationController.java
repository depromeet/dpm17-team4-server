package depromeet.lessonfour.server.notification.api.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import depromeet.lessonfour.server.common.api.code.SuccessCode;
import depromeet.lessonfour.server.common.api.dto.SuccessResponse;
import depromeet.lessonfour.server.notification.app.dto.request.SaveNotificationSettingsRequestDto;
import depromeet.lessonfour.server.notification.app.dto.request.SendNotificationRequestDto;
import depromeet.lessonfour.server.notification.app.dto.response.SaveNotificationSettingsResponseDto;
import depromeet.lessonfour.server.notification.app.dto.response.SendNotificationResponseDto;
import depromeet.lessonfour.server.notification.app.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "알림", description = "Firebase Cloud Messaging 푸시 알림 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notification")
public class NotificationController {

  private final NotificationService notificationService;

  @Operation(
      summary = "푸시 알림 전송",
      description = "Firebase Cloud Messaging을 사용하여 특정 디바이스에 푸시 알림을 전송합니다.",
      security = {@SecurityRequirement(name = "JWT")})
  @PostMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<SuccessResponse<SendNotificationResponseDto>> sendNotification(
      @Valid @RequestBody SendNotificationRequestDto requestDto,
      @AuthenticationPrincipal(expression = "id") Long userId) {

    SendNotificationResponseDto response = notificationService.sendNotification(requestDto);

    return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_SEND, response));
  }

  @Operation(
      summary = "알림 설정 저장",
      description = "사용자의 Firebase 등록 토큰과 알림 활성화 여부를 저장합니다.",
      security = {@SecurityRequirement(name = "JWT")})
  @PostMapping(
      value = "/settings",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<SuccessResponse<SaveNotificationSettingsResponseDto>>
      saveNotificationSettings(
          @Valid @RequestBody SaveNotificationSettingsRequestDto requestDto,
          @AuthenticationPrincipal(expression = "id") Long userId) {

    SaveNotificationSettingsResponseDto response =
        notificationService.saveNotificationSettings(requestDto, userId);

    return ResponseEntity.status(201)
        .body(SuccessResponse.of(SuccessCode.SUCCESS_CREATE, response));
  }
}
