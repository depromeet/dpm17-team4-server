package depromeet.lessonfour.server.auth.app.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.auth.app.dto.response.AuthTokenDto;
import depromeet.lessonfour.server.auth.domain.vo.AccountContext;
import depromeet.lessonfour.server.auth.infra.security.jwt.JwtTokenGenerator;
import depromeet.lessonfour.server.auth.infra.security.jwt.JwtTokenValidator;
import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.user.domain.entity.User;
import depromeet.lessonfour.server.user.infra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UseCase
@Transactional
@RequiredArgsConstructor
public class RefreshTokenUseCase {

  private final UserRepository userRepository;
  private final JwtTokenValidator jwtTokenValidator;
  private final JwtTokenGenerator jwtTokenGenerator;

  public AuthTokenDto refresh(String refreshToken) {
    log.info("[RefreshTokenUseCase] Starting token refresh process");

    log.debug("[RefreshTokenUseCase] Validating refresh token format and signature");
    validateRefreshToken(refreshToken);
    log.debug("[RefreshTokenUseCase] Refresh token validation passed");

    log.debug("[RefreshTokenUseCase] Extracting user ID from refresh token");
    String userId = jwtTokenValidator.extractSubject(refreshToken);
    log.debug("[RefreshTokenUseCase] User ID extracted from token: {}", userId);

    log.debug("[RefreshTokenUseCase] Looking up user by ID: {}", userId);
    User user = findUserById(Long.valueOf(userId));
    log.info(
        "[RefreshTokenUseCase] User found - userId: {}, email: {}", user.getId(), user.getEmail());

    log.debug("[RefreshTokenUseCase] Comparing provided token with stored token");
    compareWithStoredToken(user, refreshToken);
    log.debug("[RefreshTokenUseCase] Token comparison successful");

    log.debug("[RefreshTokenUseCase] Generating new token pair");
    AuthTokenDto result = generateNewToken(user);
    log.info(
        "[RefreshTokenUseCase] Token refresh completed successfully for user: {}", user.getId());

    return result;
  }

  private void validateRefreshToken(String refreshToken) {
    if (refreshToken == null) {
      log.error("[RefreshTokenUseCase] Refresh token is null");
      throw new BadCredentialsException("Invalid refresh token");
    }
    if (refreshToken.isBlank()) {
      log.error("[RefreshTokenUseCase] Refresh token is blank");
      throw new BadCredentialsException("Invalid refresh token");
    }
    if (!jwtTokenValidator.isValidToken(refreshToken)) {
      log.error(
          "[RefreshTokenUseCase] Refresh token validation failed - token is invalid or expired");
      throw new BadCredentialsException("Invalid refresh token");
    }
  }

  private User findUserById(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(
            () -> {
              log.error("[RefreshTokenUseCase] User not found for ID: {}", userId);
              return new BadCredentialsException("User not found");
            });
  }

  private void compareWithStoredToken(User user, String refreshToken) {
    if (!refreshToken.equals(user.getRefreshToken())) {
      log.error(
          "[RefreshTokenUseCase] Refresh token mismatch for user: {} - token doesn't match stored token",
          user.getId());
      throw new BadCredentialsException("Refresh token mismatch");
    }
  }

  private AuthTokenDto generateNewToken(User user) {
    log.debug("[RefreshTokenUseCase] Creating account context for user: {}", user.getId());
    AccountContext accountContext = AccountContext.of(user);

    log.debug("[RefreshTokenUseCase] Generating new access token");
    String newAccessToken = jwtTokenGenerator.generateAccessToken(accountContext);

    log.debug("[RefreshTokenUseCase] Generating new refresh token");
    String newRefreshToken = jwtTokenGenerator.generateRefreshToken(accountContext);

    log.debug("[RefreshTokenUseCase] Storing new refresh token for user: {}", user.getId());
    user.storeRefreshToken(newRefreshToken);
    userRepository.save(user);

    log.debug("[RefreshTokenUseCase] New token pair generated successfully");
    return new AuthTokenDto(newAccessToken, newRefreshToken);
  }
}
