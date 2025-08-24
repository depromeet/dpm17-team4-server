package depromeet.lessonfour.server.auth.service;

import depromeet.lessonfour.server.auth.api.dto.request.ReIssueRequestDto;
import depromeet.lessonfour.server.auth.config.jwt.JwtTokenGenerator;
import depromeet.lessonfour.server.auth.config.jwt.JwtTokenValidator;
import depromeet.lessonfour.server.auth.config.userdetails.AccountContext;
import depromeet.lessonfour.server.auth.persist.jpa.UserRepository;
import depromeet.lessonfour.server.auth.persist.jpa.entity.User;
import depromeet.lessonfour.server.auth.service.dto.ReIssueResult;
import depromeet.lessonfour.server.common.annotation.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@UseCase
@Transactional
@RequiredArgsConstructor
public class ReIssueTokenUseCase {

    private final UserRepository userRepository;
    private final JwtTokenValidator jwtTokenValidator;
    private final JwtTokenGenerator jwtTokenGenerator;

    public ReIssueResult reIssue(ReIssueRequestDto dto) {
        validateRefreshToken(dto);

        String userId = jwtTokenValidator.extractSubject(dto.refreshToken());
        User user = findUserById(userId);
        compareWithStoredToken(user, dto.refreshToken());

        return generateNewToken(user);
    }

    private void validateRefreshToken(ReIssueRequestDto dto) {
        if (!jwtTokenValidator.isValidToken(dto.refreshToken())) {
            throw new BadCredentialsException("Invalid refresh token");
        }
    }

    private User findUserById(String userId) {
        return userRepository.findById(UUID.fromString(userId))
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

        return new ReIssueResult(newAccessToken, newRefreshToken);
    }
}
