package depromeet.lessonfour.server.user.app.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.common.api.code.ErrorCode;
import depromeet.lessonfour.server.common.exception.ServerException;
import depromeet.lessonfour.server.user.app.dto.request.UpdateUserProfileRequestDto;
import depromeet.lessonfour.server.user.domain.entity.User;
import depromeet.lessonfour.server.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserUpdateService {

  private final UserRepository userRepository;

  public void updateRefreshToken(Long userId, String newRefreshToken) {
    userRepository
        .findById(userId)
        .ifPresent(
            user -> {
              user.storeRefreshToken(newRefreshToken);
            });
  }

  public User updateUserProfile(Long userId, UpdateUserProfileRequestDto requestDto) {
    User user = userRepository
        .findActiveById(userId)
        .orElseThrow(() -> new ServerException(ErrorCode.USER_NOT_FOUND));
    
    user.updateProfile(
        requestDto.nickname(),
        requestDto.profileImage(),
        requestDto.gender(),
        requestDto.birthYear()
    );
    return user;
  }
}
