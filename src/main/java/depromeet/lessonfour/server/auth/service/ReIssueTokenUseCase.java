package depromeet.lessonfour.server.auth.service;

import java.util.UUID;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.auth.persist.jpa.UserRepository;
import depromeet.lessonfour.server.auth.persist.jpa.entity.User;
import depromeet.lessonfour.server.auth.security.jwt.JwtTokenGenerator;
import depromeet.lessonfour.server.auth.security.jwt.JwtTokenValidator;
import depromeet.lessonfour.server.auth.security.userdetails.AccountContext;
import depromeet.lessonfour.server.auth.service.dto.ReIssueResult;
import depromeet.lessonfour.server.common.annotation.UseCase;
import lombok.RequiredArgsConstructor;

@UseCase
@Transactional
@RequiredArgsConstructor
public class ReIssueTokenUseCase {

  private final UserRepository userRepository;
  private final JwtTokenValidator jwtTokenValidator;
  private final JwtTokenGenerator jwtTokenGenerator;

  public ReIssueResult reIssue(String refreshToken) {
    validateRefreshToken(refreshToken);

    String userId = jwtTokenValidator.extractSubject(refreshToken);
    User user = findUserById(userId);
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

  private User findUserById(String userId) {
    final UUID uuid;
    try {
      uuid = UUID.fromString(userId);
    } catch (IllegalArgumentException e) {
      throw new BadCredentialsException("Invalid refresh token subject");
    }
    return userRepository
        .findById(uuid)
        .orElseThrow(() -> new BadCredentialsException("User not found"));
  }

  private void compareWithStoredToken(User user, String refreshToken) {
    if (!refreshToken.equals(user.getRefreshToken())) {
      throw new BadCredentialsException("Refresh token mismatch");
    }
  }

  private ReIssueResult generateNewToken(User user) {
    AccountContext accountContext = AccountContext.of(user);
    String newAccessToken = jwtTokenGenerator.generateAccessToken(accountContext);
    String newRefreshToken = jwtTokenGenerator.generateRefreshToken(accountContext);
    user.storeRefreshToken(newRefreshToken);
    userRepository.save(user);
    return new ReIssueResult(newAccessToken, newRefreshToken);
  }
}
