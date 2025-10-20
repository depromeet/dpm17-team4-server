package depromeet.lessonfour.server.notification.app.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.SendResponse;

import depromeet.lessonfour.server.common.api.code.ErrorCode;
import depromeet.lessonfour.server.common.exception.ServerException;
import depromeet.lessonfour.server.notification.app.dto.request.SaveNotificationSettingRequestDto;
import depromeet.lessonfour.server.notification.app.dto.request.SendNotificationRequestDto;
import depromeet.lessonfour.server.notification.app.dto.response.SaveNotificationSettingResponseDto;
import depromeet.lessonfour.server.notification.app.dto.response.SendNotificationResponseDto;
import depromeet.lessonfour.server.notification.domain.entity.NotificationSetting;
import depromeet.lessonfour.server.notification.domain.repository.NotificationSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationSettingRepository notificationSettingRepository;

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
  public SaveNotificationSettingResponseDto saveNotificationSetting(
      SaveNotificationSettingRequestDto requestDto, Long userId) {
    // 이미 존재하는 토큰인지 확인
    if (notificationSettingRepository.existsByRegistrationToken(requestDto.registrationToken())) {
      throw new ServerException(ErrorCode.CONFLICT);
    }

    // NotificationSetting 엔티티 생성 및 저장
    NotificationSetting notificationSetting =
        NotificationSetting.create(userId, requestDto.registrationToken(), requestDto.enabled());
    NotificationSetting saved = notificationSettingRepository.save(notificationSetting);

    // 응답 DTO 생성
    return new SaveNotificationSettingResponseDto(
        saved.getId(), saved.getUserId(), saved.getRegistrationToken(), saved.getEnabled());
  }
}
