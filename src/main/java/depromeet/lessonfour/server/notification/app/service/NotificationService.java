package depromeet.lessonfour.server.notification.app.service;

import java.util.ArrayList;
import java.util.Arrays;
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
import depromeet.lessonfour.server.notification.app.dto.response.SaveNotificationSettingsResponseDto;
import depromeet.lessonfour.server.notification.app.dto.response.SendNotificationResponseDto;
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
    List<String> registrationTokens =
        Arrays.asList("YOUR_REGISTRATION_TOKEN_1", "YOUR_REGISTRATION_TOKEN_n");
    MulticastMessage message =
        MulticastMessage.builder()
            .putData("title", requestDto.title())
            .putData("body", requestDto.body())
            .addAllTokens(registrationTokens)
            .build();

    BatchResponse response;
    try {
      response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
    } catch (FirebaseMessagingException e) {
      log.error("Failed to send FCM message", e);
      throw new RuntimeException("Failed to send push notification: " + e.getMessage(), e);
    }
    if (response.getFailureCount() > 0) {
      List<SendResponse> responses = response.getResponses();
      List<String> failedTokens = new ArrayList<>();
      for (int i = 0; i < responses.size(); i++) {
        if (!responses.get(i).isSuccessful()) {
          // The order of responses corresponds to the order of the registration tokens.
          failedTokens.add(registrationTokens.get(i));
        }
      }
      log.error("List of tokens that caused failures: " + failedTokens);
    }
    return new SendNotificationResponseDto(response.getSuccessCount(), response.getFailureCount());
  }

  @Transactional
  public SaveNotificationSettingsResponseDto saveNotificationSettings(
      SaveNotificationSettingsRequestDto requestDto, Long userId) {
    // 이미 존재하는 토큰인지 확인
    if (notificationSettingsRepository.existsByRegistrationToken(requestDto.registrationToken())) {
      throw new ServerException(ErrorCode.CONFLICT);
    }

    // NotificationSettings 엔티티 생성 및 저장
    NotificationSettings notificationSettings =
        NotificationSettings.create(userId, requestDto.registrationToken(), requestDto.enabled());
    NotificationSettings saved = notificationSettingsRepository.save(notificationSettings);

    // 응답 DTO 생성
    return new SaveNotificationSettingsResponseDto(
        saved.getId(), saved.getUserId(), saved.getRegistrationToken(), saved.getEnabled());
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
}
