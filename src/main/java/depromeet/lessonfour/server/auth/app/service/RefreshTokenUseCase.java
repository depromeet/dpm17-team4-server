package depromeet.lessonfour.server.auth.app.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.auth.app.dto.response.AuthTokenDto;
import depromeet.lessonfour.server.auth.domain.vo.AccountContext;
import depromeet.lessonfour.server.auth.infra.security.jwt.JwtTokenGenerator;
import depromeet.lessonfour.server.auth.infra.security.jwt.JwtTokenValidator;
import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.user.domain.User;
import depromeet.lessonfour.server.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;

@UseCase
@Transactional
@RequiredArgsConstructor
public class RefreshTokenUseCase {

  private final UserRepository userRepository;
  private final JwtTokenValidator jwtTokenValidator;
  private final JwtTokenGenerator jwtTokenGenerator;

  public AuthTokenDto refresh(String refreshToken) {
    validateRefreshToken(refreshToken);

    String userId = jwtTokenValidator.extractSubject(refreshToken);
    User user = findUserById(Long.valueOf(userId));
    compareWithStoredToken(user, refreshToken);

    return generateNewToken(user);
  }

  private void validateRefreshToken(String refreshToken) {
    if (refreshToken == null
        || refreshToken.isBlank()
        || !jwtTokenValidator.isValidToken(refreshToken)) {
      throw new BadCredentialsException("Invalid refresh token");
    }
  }

  private User findUserById(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new BadCredentialsException("User not found"));
  }

  private void compareWithStoredToken(User user, String refreshToken) {
    if (!refreshToken.equals(user.getRefreshToken())) {
      throw new BadCredentialsException("Refresh token mismatch");
    }
  }

  private AuthTokenDto generateNewToken(User user) {
    AccountContext accountContext = AccountContext.of(user);
    String newAccessToken = jwtTokenGenerator.generateAccessToken(accountContext);
    String newRefreshToken = jwtTokenGenerator.generateRefreshToken(accountContext);
    user.storeRefreshToken(newRefreshToken);
    userRepository.save(user);
    return new AuthTokenDto(newAccessToken, newRefreshToken);
  }
}
