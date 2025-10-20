package depromeet.lessonfour.server.notification.api.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import depromeet.lessonfour.server.common.api.code.SuccessCode;
import depromeet.lessonfour.server.common.api.dto.SuccessResponse;
import depromeet.lessonfour.server.notification.app.dto.request.SaveNotificationSettingsRequestDto;
import depromeet.lessonfour.server.notification.app.dto.request.SendNotificationRequestDto;
import depromeet.lessonfour.server.notification.app.dto.request.UpdateNotificationSettingsRequestDto;
import depromeet.lessonfour.server.notification.app.dto.response.GetNotificationSettingsResponseDto;
import depromeet.lessonfour.server.notification.app.dto.response.SaveNotificationSettingsResponseDto;
import depromeet.lessonfour.server.notification.app.dto.response.SendNotificationResponseDto;
import depromeet.lessonfour.server.notification.app.dto.response.UpdateNotificationSettingsResponseDto;
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

  @Operation(
      summary = "알림 설정 목록 조회",
      description = "사용자의 모든 알림 설정 목록을 조회합니다.",
      security = {@SecurityRequirement(name = "JWT")})
  @GetMapping(value = "/settings", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<SuccessResponse<List<GetNotificationSettingsResponseDto>>>
      getAllNotificationSettings(@AuthenticationPrincipal(expression = "id") Long userId) {

    List<GetNotificationSettingsResponseDto> response =
        notificationService.getAllNotificationSettings(userId);

    return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_FETCH, response));
  }

  @Operation(
      summary = "알림 설정 조회",
      description = "특정 알림 설정을 조회합니다.",
      security = {@SecurityRequirement(name = "JWT")})
  @GetMapping(value = "/settings/{settingsId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<SuccessResponse<GetNotificationSettingsResponseDto>>
      getNotificationSettings(
          @PathVariable Long settingsId, @AuthenticationPrincipal(expression = "id") Long userId) {

    GetNotificationSettingsResponseDto response =
        notificationService.getNotificationSettings(settingsId, userId);

    return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_FETCH, response));
  }

  @Operation(
      summary = "알림 설정 업데이트",
      description = "등록된 알림 설정의 활성화 여부를 업데이트합니다.",
      security = {@SecurityRequirement(name = "JWT")})
  @PatchMapping(
      value = "/settings/{settingsId}",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<SuccessResponse<UpdateNotificationSettingsResponseDto>>
      updateNotificationSettings(
          @PathVariable Long settingsId,
          @Valid @RequestBody UpdateNotificationSettingsRequestDto requestDto,
          @AuthenticationPrincipal(expression = "id") Long userId) {

    UpdateNotificationSettingsResponseDto response =
        notificationService.updateNotificationSettings(settingsId, requestDto, userId);

    return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_UPDATE, response));
  }
}
