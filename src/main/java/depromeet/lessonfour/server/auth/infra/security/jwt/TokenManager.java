package depromeet.lessonfour.server.auth.infra.security.jwt;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.auth.app.dto.response.AuthResponseDto;
import depromeet.lessonfour.server.auth.app.dto.response.TokenPairDto;
import depromeet.lessonfour.server.auth.domain.vo.AccountContext;
import depromeet.lessonfour.server.user.app.service.UserUpdateService;
import depromeet.lessonfour.server.user.domain.entity.User;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class TokenManager {

  private final JwtTokenGenerator jwtTokenGenerator;
  private final UserUpdateService userUpdateService;
  private final JwtTokenValidator jwtTokenValidator;

  public TokenPairDto generateAndStoreTokens(User user, boolean includeAccessToken) {
    // 기존 refresh token이 유효하면 재사용
    String refreshToken = user.getRefreshToken();
    if (refreshToken == null || !jwtTokenValidator.isValidToken(refreshToken)) {
      refreshToken = jwtTokenGenerator.generateRefreshToken(AccountContext.of(user));
      user.storeRefreshToken(refreshToken);
    }

    String accessToken = null;
    if (includeAccessToken) {
      accessToken = jwtTokenGenerator.generateAccessToken(AccountContext.of(user));
    }

    return new TokenPairDto(accessToken, refreshToken);
  }

  public String generateAndStoreRefreshToken(AuthResponseDto authResponse) {
    AccountContext accountContext =
        AccountContext.from(authResponse.id(), authResponse.email(), authResponse.nickname());
    String refreshToken = jwtTokenGenerator.generateRefreshToken(accountContext);
    userUpdateService.updateRefreshToken(authResponse.id(), refreshToken);

    return refreshToken;
  }
}
