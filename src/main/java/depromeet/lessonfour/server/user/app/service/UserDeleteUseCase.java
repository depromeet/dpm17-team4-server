package depromeet.lessonfour.server.user.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.common.api.code.ErrorCode;
import depromeet.lessonfour.server.common.exception.ServerException;
import depromeet.lessonfour.server.user.domain.entity.User;
import depromeet.lessonfour.server.user.domain.repository.UserRepository;
import depromeet.lessonfour.server.user.domain.vo.Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UseCase
@Transactional
@RequiredArgsConstructor
public class UserDeleteUseCase {

  private final UserRepository userRepository;
  private final RestTemplate restTemplate = new RestTemplate();
  
  @Value("${spring.security.oauth2.client.registration.kakao.admin-key}")
  private String kakaoAdminKey;
  
  @Value("${spring.security.oauth2.client.registration.kakao.unlink-uri}")
  private String kakaoUnlinkUri;

  public void delete(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ServerException(ErrorCode.USER_NOT_FOUND));

    if (user.isDeleted()) {
      return;
    }

    // Kakao 사용자인 경우 Kakao API 호출
    if (user.getProvider() != null && user.getProvider().getType() == Provider.ProviderType.KAKAO) {
      unlinkKakaoUser(user.getProvider().getId());
    }

    // DB에서 사용자 비활성화 및 refresh token 제거
    user.deactivate();
    user.storeRefreshToken(null);

    log.info("User {} has been successfully deleted", userId);
  }

  private void unlinkKakaoUser(String kakaoUserId) {
    log.debug("kakaoUserId = " + kakaoUserId);
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.set("Authorization", "KakaoAK " + kakaoAdminKey);
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

      MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
      requestBody.add("target_id_type", "user_id");
      requestBody.add("target_id", kakaoUserId);
      
      HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(requestBody, headers);

      ResponseEntity<String> response =
          restTemplate.exchange(kakaoUnlinkUri, HttpMethod.POST, entity, String.class);

      if (response.getStatusCode().is2xxSuccessful()) {
        log.info("Kakao user unlink successful for user {}: {}", kakaoUserId, response.getBody());
        
      } else {
        log.warn(
            "Kakao user unlink failed for user {} with status: {}",
            kakaoUserId,
            response.getStatusCode());
      }
    } catch (Exception e) {
      log.error("Failed to unlink Kakao user {}", kakaoUserId, e);
      // Kakao 연동 실패해도 DB 삭제는 진행 (사용자 요청이므로)
    }
  }
}
