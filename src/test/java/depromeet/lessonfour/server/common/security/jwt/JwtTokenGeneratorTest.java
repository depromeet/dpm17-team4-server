package depromeet.lessonfour.server.common.security.jwt;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.lenient;

import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import depromeet.lessonfour.server.auth.config.jwt.JwtSecretKeyProvider;
import depromeet.lessonfour.server.auth.config.jwt.JwtTokenGenerator;
import depromeet.lessonfour.server.auth.config.userdetails.AccountContext;
import depromeet.lessonfour.server.auth.persist.jpa.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@ExtendWith(MockitoExtension.class)
class JwtTokenGeneratorTest {

  @Mock private JwtSecretKeyProvider secretKeyProvider;

  @InjectMocks private JwtTokenGenerator jwtTokenGenerator;

  private SecretKey testSecretKey;

  @BeforeEach
  void setUp() {
    testSecretKey =
        Keys.hmacShaKeyFor(
            "testSecretKeyForJWTWhichShouldBeAtLeast256BitsLongForSecurity".getBytes());
    lenient().when(secretKeyProvider.getSecretKey()).thenReturn(testSecretKey);

    ReflectionTestUtils.setField(jwtTokenGenerator, "accessTokenExpirationSeconds", 3600L);
  }

  @Nested
  @DisplayName("Access Token 생성 테스트")
  class GenerateAccessTokenTest {

    @Test
    @DisplayName("유효한 사용자로 Access Token을 생성할 수 있다")
    void givenValidUser_whenGenerateAccessToken_thenReturnValidToken() {
      // given
      User user = User.register("test@example.com", "testuser", "password123");
      ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
      AccountContext accountContext = AccountContext.of(user);

      // when
      String accessToken = jwtTokenGenerator.generateAccessToken(accountContext);

      // then
      assertThat(accessToken).isNotNull();
      assertThat(accessToken).isNotEmpty();

      Claims claims =
          Jwts.parser()
              .verifyWith(testSecretKey)
              .build()
              .parseSignedClaims(accessToken)
              .getPayload();

      assertThat(claims.getSubject()).isEqualTo(user.getId().toString());
      assertThat(claims.get("role")).isEqualTo(user.getAuthority());
      assertThat(claims.get("email")).isEqualTo(user.getEmail());
      assertThat(claims.get("nickname")).isEqualTo(user.getNickname());
    }

    @Test
    @DisplayName("Access Token에는 올바른 만료 시간이 설정된다")
    void givenValidUser_whenGenerateAccessToken_thenExpirationTimeIsCorrect() {
      // given
      User user = User.register("test@example.com", "testuser", "password123");
      ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
      AccountContext accountContext = AccountContext.of(user);

      // when
      long beforeGeneration = System.currentTimeMillis();
      String accessToken = jwtTokenGenerator.generateAccessToken(accountContext);
      long afterGeneration = System.currentTimeMillis();

      // then
      Claims claims =
          Jwts.parser()
              .verifyWith(testSecretKey)
              .build()
              .parseSignedClaims(accessToken)
              .getPayload();

      Date expiration = claims.getExpiration();
      Date issuedAt = claims.getIssuedAt();

      long expectedExpirationMillis = 3600L * 1000L;
      long actualExpirationDiff = expiration.getTime() - issuedAt.getTime();

      assertThat(actualExpirationDiff).isEqualTo(expectedExpirationMillis);
      assertThat(issuedAt.getTime()).isBetween(beforeGeneration - 1000, afterGeneration + 1000);
    }

    @Test
    @DisplayName("null 사용자로 Access Token 생성 시 예외가 발생한다")
    void givenNullUser_whenGenerateAccessToken_thenThrowException() {
      // given
      JwtTokenGenerator generator = new JwtTokenGenerator(secretKeyProvider);
      ReflectionTestUtils.setField(generator, "accessTokenExpirationSeconds", 3600L);

      // when & then
      assertThatThrownBy(() -> generator.generateAccessToken(null))
          .isInstanceOf(NullPointerException.class);
    }
  }

  @Nested
  @DisplayName("Refresh Token 생성 테스트")
  class GenerateRefreshTokenTest {

    @Test
    @DisplayName("유효한 사용자로 Refresh Token을 생성할 수 있다")
    void givenValidUser_whenGenerateRefreshToken_thenReturnValidToken() {
      // given
      User user = User.register("test@example.com", "testuser", "password123");
      ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
      AccountContext accountContext = AccountContext.of(user);

      // when
      String refreshToken = jwtTokenGenerator.generateRefreshToken(accountContext);

      // then
      assertThat(refreshToken).isNotNull();
      assertThat(refreshToken).isNotEmpty();

      Claims claims =
          Jwts.parser()
              .verifyWith(testSecretKey)
              .build()
              .parseSignedClaims(refreshToken)
              .getPayload();

      assertThat(claims.getSubject()).isEqualTo(user.getId().toString());
      assertThat(claims.getId()).isNotNull();
      assertThat(claims.getId()).isNotEmpty();
    }

    @Test
    @DisplayName("Refresh Token에는 7일의 만료 시간이 설정된다")
    void givenValidUser_whenGenerateRefreshToken_thenExpirationTimeIsSevenDays() {
      // given
      User user = User.register("test@example.com", "testuser", "password123");
      ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
      AccountContext accountContext = AccountContext.of(user);

      // when
      long beforeGeneration = System.currentTimeMillis();
      String refreshToken = jwtTokenGenerator.generateRefreshToken(accountContext);
      long afterGeneration = System.currentTimeMillis();

      // then
      Claims claims =
          Jwts.parser()
              .verifyWith(testSecretKey)
              .build()
              .parseSignedClaims(refreshToken)
              .getPayload();

      Date expiration = claims.getExpiration();
      Date issuedAt = claims.getIssuedAt();

      long expectedExpirationMillis = 3600L * 1000L * 7L;
      long actualExpirationDiff = expiration.getTime() - issuedAt.getTime();

      assertThat(actualExpirationDiff).isEqualTo(expectedExpirationMillis);
      assertThat(issuedAt.getTime()).isBetween(beforeGeneration - 1000, afterGeneration + 1000);
    }

    @Test
    @DisplayName("Refresh Token에는 Access Token과 달리 사용자 정보 클레임이 포함되지 않는다")
    void givenValidUser_whenGenerateRefreshToken_thenDoesNotContainUserInfoClaims() {
      // given
      User user = User.register("test@example.com", "testuser", "password123");
      ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
      AccountContext accountContext = AccountContext.of(user);

      // when
      String refreshToken = jwtTokenGenerator.generateRefreshToken(accountContext);

      // then
      Claims claims =
          Jwts.parser()
              .verifyWith(testSecretKey)
              .build()
              .parseSignedClaims(refreshToken)
              .getPayload();

      assertThat(claims.get("role")).isNull();
      assertThat(claims.get("email")).isNull();
      assertThat(claims.get("nickname")).isNull();
      assertThat(claims.get("userId")).isNull();
    }

    @Test
    @DisplayName("Refresh Token에는 고유한 JTI가 포함된다")
    void givenValidUser_whenGenerateRefreshToken_thenContainsUniqueJti() {
      // given
      User user = User.register("test@example.com", "testuser", "password123");
      ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
      AccountContext accountContext = AccountContext.of(user);

      // when
      String refreshToken1 = jwtTokenGenerator.generateRefreshToken(accountContext);
      String refreshToken2 = jwtTokenGenerator.generateRefreshToken(accountContext);

      // then
      Claims claims1 =
          Jwts.parser()
              .verifyWith(testSecretKey)
              .build()
              .parseSignedClaims(refreshToken1)
              .getPayload();

      Claims claims2 =
          Jwts.parser()
              .verifyWith(testSecretKey)
              .build()
              .parseSignedClaims(refreshToken2)
              .getPayload();

      assertThat(claims1.getId()).isNotEqualTo(claims2.getId());
      assertThat(claims1.getId())
          .matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    }

    @Test
    @DisplayName("null 사용자로 Refresh Token 생성 시 예외가 발생한다")
    void givenNullUser_whenGenerateRefreshToken_thenThrowException() {
      // given
      JwtTokenGenerator generator = new JwtTokenGenerator(secretKeyProvider);
      ReflectionTestUtils.setField(generator, "accessTokenExpirationSeconds", 3600L);

      // when & then
      assertThatThrownBy(() -> generator.generateRefreshToken(null))
          .isInstanceOf(NullPointerException.class);
    }
  }

  @Nested
  @DisplayName("토큰 만료 시간 설정 테스트")
  class ExpirationConfigurationTest {

    @Test
    @DisplayName("커스텀 만료 시간 설정이 정상적으로 적용된다")
    void givenCustomExpirationTime_whenGenerateTokens_thenExpirationTimeIsCorrect() {
      // given
      ReflectionTestUtils.setField(jwtTokenGenerator, "accessTokenExpirationSeconds", 7200L);

      User user = User.register("test@example.com", "testuser", "password123");
      ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
      AccountContext accountContext = AccountContext.of(user);

      // when
      String accessToken = jwtTokenGenerator.generateAccessToken(accountContext);
      String refreshToken = jwtTokenGenerator.generateRefreshToken(accountContext);

      // then
      Claims accessClaims =
          Jwts.parser()
              .verifyWith(testSecretKey)
              .build()
              .parseSignedClaims(accessToken)
              .getPayload();

      Claims refreshClaims =
          Jwts.parser()
              .verifyWith(testSecretKey)
              .build()
              .parseSignedClaims(refreshToken)
              .getPayload();

      long accessTokenDuration =
          accessClaims.getExpiration().getTime() - accessClaims.getIssuedAt().getTime();
      long refreshTokenDuration =
          refreshClaims.getExpiration().getTime() - refreshClaims.getIssuedAt().getTime();

      assertThat(accessTokenDuration).isEqualTo(7200L * 1000L);
      assertThat(refreshTokenDuration).isEqualTo(7200L * 1000L * 7L);
    }
  }

  @Nested
  @DisplayName("토큰 서명 검증 테스트")
  class TokenSignatureTest {

    @Test
    @DisplayName("생성된 토큰이 올바른 시크릿 키로 서명되었는지 검증한다")
    void givenGeneratedTokens_whenVerifySignature_thenVerificationSucceeds() {
      // given
      User user = User.register("test@example.com", "testuser", "password123");
      ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
      AccountContext accountContext = AccountContext.of(user);

      // when
      String accessToken = jwtTokenGenerator.generateAccessToken(accountContext);
      String refreshToken = jwtTokenGenerator.generateRefreshToken(accountContext);

      // then
      assertThatNoException()
          .isThrownBy(
              () -> Jwts.parser().verifyWith(testSecretKey).build().parseSignedClaims(accessToken));

      assertThatNoException()
          .isThrownBy(
              () ->
                  Jwts.parser().verifyWith(testSecretKey).build().parseSignedClaims(refreshToken));
    }

    @Test
    @DisplayName("잘못된 시크릿 키로 토큰 검증 시 예외가 발생한다")
    void givenGeneratedToken_whenVerifyWithWrongKey_thenThrowException() {
      // given
      User user = User.register("test@example.com", "testuser", "password123");
      ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
      AccountContext accountContext = AccountContext.of(user);

      String accessToken = jwtTokenGenerator.generateAccessToken(accountContext);
      SecretKey wrongKey =
          Keys.hmacShaKeyFor("wrongSecretKeyForTestingPurposesWhichIsAlsoLongEnough".getBytes());

      // when & then
      assertThatThrownBy(
              () -> Jwts.parser().verifyWith(wrongKey).build().parseSignedClaims(accessToken))
          .isInstanceOf(io.jsonwebtoken.security.SignatureException.class);
    }
  }
}
