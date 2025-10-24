package depromeet.lessonfour.server.user.app.service;

import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.auth.infra.security.oauth.kakao.KakaoAdminClient;
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
@RequiredArgsConstructor
public class UserDeleteUseCase {

  private final UserRepository userRepository;
  private final KakaoAdminClient kakaoAdminClient;

  @Transactional
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
      try {
        kakaoAdminClient.unlinkUser(user.getProvider().getId());
      } catch (Exception e) {
        log.error("Failed to unlink Kakao user {}", user.getProvider().getId(), e);
        // Kakao 연동 실패해도 DB 삭제는 진행 (사용자 요청이므로)
      }
    }

    // DB에서 사용자 비활성화 및 refresh token 제거
    user.deactivate();
    user.storeRefreshToken(null);

    log.info("User {} has been successfully deleted", userId);
  }
}
