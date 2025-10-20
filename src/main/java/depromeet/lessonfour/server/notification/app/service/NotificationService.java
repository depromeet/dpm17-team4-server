package depromeet.lessonfour.server.notification.app.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.SendResponse;

import depromeet.lessonfour.server.common.api.code.ErrorCode;
import depromeet.lessonfour.server.common.exception.ServerException;
import depromeet.lessonfour.server.notification.app.dto.request.SaveNotificationSettingsRequestDto;
import depromeet.lessonfour.server.notification.app.dto.request.SendNotificationRequestDto;
import depromeet.lessonfour.server.notification.app.dto.request.UpdateNotificationSettingsRequestDto;
import depromeet.lessonfour.server.notification.app.dto.response.GetNotificationSettingsResponseDto;
import depromeet.lessonfour.server.notification.app.dto.response.SaveNotificationSettingsResponseDto;
import depromeet.lessonfour.server.notification.app.dto.response.SendNotificationResponseDto;
import depromeet.lessonfour.server.notification.app.dto.response.UpdateNotificationSettingsResponseDto;
import depromeet.lessonfour.server.notification.domain.entity.NotificationSettings;
import depromeet.lessonfour.server.notification.domain.repository.NotificationSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationSettingsRepository notificationSettingsRepository;

  public SendNotificationResponseDto sendNotification(SendNotificationRequestDto requestDto) {
    int totalSuccessCount = 0;
    int totalFailureCount = 0;
    int page = 0;

    while (true) {
      // 500개씩 페이지네이션으로 토큰 조회
      List<String> registrationTokens = getEnabledRegistrationTokens(page);

      // 더 이상 토큰이 없으면 종료
      if (registrationTokens.isEmpty()) {
        break;
      }

      log.info("Sending notification to {} tokens (page: {})", registrationTokens.size(), page);

      // FCM multicast 메시지 생성
      MulticastMessage message =
          MulticastMessage.builder()
              .putData("title", requestDto.title())
              .putData("body", requestDto.body())
              .addAllTokens(registrationTokens)
              .build();

      // FCM 전송
      BatchResponse response;
      try {
        response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
      } catch (FirebaseMessagingException e) {
        log.error("Failed to send FCM message for page {}", page, e);
        throw new RuntimeException("Failed to send push notification: " + e.getMessage(), e);
      }

      // 성공/실패 카운트 누적
      totalSuccessCount += response.getSuccessCount();
      totalFailureCount += response.getFailureCount();

      // 실패한 토큰 로깅
      if (response.getFailureCount() > 0) {
        List<SendResponse> responses = response.getResponses();
        List<String> failedTokens = new ArrayList<>();
        for (int i = 0; i < responses.size(); i++) {
          if (!responses.get(i).isSuccessful()) {
            // The order of responses corresponds to the order of the registration tokens.
            failedTokens.add(registrationTokens.get(i));
          }
        }
        log.error("List of tokens that caused failures (page {}): {}", page, failedTokens);
      }

      page++;
    }

    log.info(
        "Notification sending completed. Total success: {}, Total failure: {}",
        totalSuccessCount,
        totalFailureCount);

    return new SendNotificationResponseDto(totalSuccessCount, totalFailureCount);
  }

  @Transactional
  public SaveNotificationSettingsResponseDto saveNotificationSettings(
      SaveNotificationSettingsRequestDto requestDto, Long userId) {
    // 같은 userId에서 동일한 key가 이미 존재하는지 확인
    if (notificationSettingsRepository.existsByUserIdAndKey(userId, requestDto.key())) {
      throw new ServerException(ErrorCode.CONFLICT);
    }

    // 이미 존재하는 토큰인지 확인
    if (notificationSettingsRepository.existsByRegistrationToken(requestDto.registrationToken())) {
      throw new ServerException(ErrorCode.CONFLICT);
    }

    // NotificationSettings 엔티티 생성 및 저장
    NotificationSettings notificationSettings =
        NotificationSettings.create(
            userId, requestDto.key(), requestDto.registrationToken(), requestDto.enabled());
    NotificationSettings saved = notificationSettingsRepository.save(notificationSettings);

    // 응답 DTO 생성
    return new SaveNotificationSettingsResponseDto(
        saved.getId(),
        saved.getUserId(),
        saved.getKey(),
        saved.getRegistrationToken(),
        saved.getEnabled());
  }

  @Transactional
  public UpdateNotificationSettingsResponseDto updateNotificationSettings(
      Long settingsId, UpdateNotificationSettingsRequestDto requestDto, Long userId) {
    // id로 설정 조회
    NotificationSettings settings =
        notificationSettingsRepository
            .findById(settingsId)
            .orElseThrow(() -> new ServerException(ErrorCode.DATA_NOT_FOUND));

    // userId 검증 (본인의 설정인지 확인)
    if (!settings.getUserId().equals(userId)) {
      throw new ServerException(ErrorCode.ACCESS_DENIED);
    }

    // key가 제공된 경우에만 업데이트
    if (requestDto.key() != null && !requestDto.key().isBlank()) {
      // key가 변경되는 경우, 중복 체크
      if (!settings.getKey().equals(requestDto.key())) {
        if (notificationSettingsRepository.existsByUserIdAndKey(userId, requestDto.key())) {
          throw new ServerException(ErrorCode.CONFLICT);
        }
        settings.updateKey(requestDto.key());
      }
    }

    // enabled가 제공된 경우에만 업데이트
    if (requestDto.enabled() != null) {
      settings.updateEnabled(requestDto.enabled());
    }

    // 응답 DTO 생성
    return new UpdateNotificationSettingsResponseDto(
        settings.getId(),
        settings.getUserId(),
        settings.getKey(),
        settings.getRegistrationToken(),
        settings.getEnabled());
  }

  /**
   * 알림이 활성화된 등록 토큰 목록을 페이지 단위로 조회합니다. FCM multicast는 최대 500개 토큰을 지원합니다.
   *
   * @param page 페이지 번호 (0부터 시작)
   * @param size 페이지 크기 (500을 초과하면 자동으로 500으로 제한됨)
   * @return enabled=true인 registrationToken 목록
   * @throws IllegalArgumentException page 또는 size가 음수인 경우
   */
  public List<String> getEnabledRegistrationTokens(int page, int size) {
    if (page < 0) {
      throw new IllegalArgumentException("페이지 번호는 음수일 수 없습니다: " + page);
    }
    if (size < 0) {
      throw new IllegalArgumentException("페이지 크기는 음수일 수 없습니다: " + size);
    }

    // FCM multicast 제한: 최대 500개
    int effectiveSize = Math.min(size, 500);
    Pageable pageable = PageRequest.of(page, effectiveSize);
    Page<NotificationSettings> settingsPage =
        notificationSettingsRepository.findByEnabled(true, pageable);
    return settingsPage.getContent().stream()
        .map(NotificationSettings::getRegistrationToken)
        .toList();
  }

  /**
   * 알림이 활성화된 등록 토큰 목록을 페이지 단위로 조회합니다. 페이지 크기는 기본값 500개입니다.
   *
   * @param page 페이지 번호 (0부터 시작)
   * @return enabled=true인 registrationToken 목록 (최대 500개)
   */
  public List<String> getEnabledRegistrationTokens(int page) {
    return getEnabledRegistrationTokens(page, 500);
  }

  /**
   * 사용자의 모든 알림 설정 목록을 조회합니다.
   *
   * @param userId 사용자 ID
   * @return 사용자의 알림 설정 목록 (빈 리스트 가능)
   */
  public List<GetNotificationSettingsResponseDto> getAllNotificationSettings(Long userId) {
    List<NotificationSettings> settingsList =
        notificationSettingsRepository.findAllByUserId(userId);
    return settingsList.stream()
        .map(
            settings ->
                new GetNotificationSettingsResponseDto(
                    settings.getId(),
                    settings.getUserId(),
                    settings.getKey(),
                    settings.getRegistrationToken(),
                    settings.getEnabled()))
        .toList();
  }

  /**
   * 특정 알림 설정을 조회합니다.
   *
   * @param settingsId 알림 설정 ID
   * @param userId 요청한 사용자 ID
   * @return 알림 설정 정보
   * @throws ServerException 설정이 존재하지 않거나 권한이 없는 경우
   */
  public GetNotificationSettingsResponseDto getNotificationSettings(Long settingsId, Long userId) {
    // settingsId로 설정 조회
    NotificationSettings settings =
        notificationSettingsRepository
            .findById(settingsId)
            .orElseThrow(() -> new ServerException(ErrorCode.DATA_NOT_FOUND));

    // userId 검증 (본인의 설정인지 확인)
    if (!settings.getUserId().equals(userId)) {
      throw new ServerException(ErrorCode.ACCESS_DENIED);
    }

    return new GetNotificationSettingsResponseDto(
        settings.getId(),
        settings.getUserId(),
        settings.getKey(),
        settings.getRegistrationToken(),
        settings.getEnabled());
  }
}
