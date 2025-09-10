package depromeet.lessonfour.server.auth.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import depromeet.lessonfour.server.auth.persist.jpa.UserRepository;
import depromeet.lessonfour.server.auth.persist.jpa.entity.User;
import depromeet.lessonfour.server.auth.security.jwt.JwtTokenGenerator;
import io.restassured.RestAssured;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql(
    scripts = "/sql/cleanup.sql",
    config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED),
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class JwtAuthenticationE2ETest {

  @LocalServerPort private int port;

  @Autowired private JwtTokenGenerator jwtTokenGenerator;
  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
  }

  @Test
  @DisplayName("유효한 JWT 토큰으로 보호된 엔드포인트 접근시 성공한다")
  void givenValidJwtToken_whenAccessProtectedEndpoint_thenSuccess() {
    // Given: 유효한 사용자와 JWT 토큰 생성
    User user = createTestUser("test@example.com", "password123", "testuser");
    String validToken =
        jwtTokenGenerator.generateAccessToken(
            user.getId(), user.getEmail(), user.getNickname(), user.getRole().name());

    // When & Then: 보호된 엔드포인트에 유효한 토큰으로 접근
    given()
        .header("Authorization", "Bearer " + validToken)
        .when()
        .get("/api/test/protected")
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("message", containsString("Protected endpoint accessed successfully"))
        .body("user", equalTo(user.getEmail()));
  }

  @Test
  @DisplayName("만료된 JWT 토큰으로 보호된 엔드포인트 접근시 401을 반환한다")
  void givenExpiredJwtToken_whenAccessProtectedEndpoint_thenUnauthorized() {
    // Given: 만료된 JWT 토큰 생성
    User user = createTestUser("expired@example.com", "password123", "expireduser");
    String expiredToken =
        jwtTokenGenerator.generateAccessToken(
            user.getId(),
            user.getEmail(),
            user.getNickname(),
            user.getRole().name(),
            Instant.now().minus(1, ChronoUnit.HOURS)); // 1시간 전에 만료

    // When & Then: 보호된 엔드포인트에 만료된 토큰으로 접근
    given()
        .header("Authorization", "Bearer " + expiredToken)
        .when()
        .get("/api/test/protected")
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(401))
        .body("error", equalTo("Unauthorized"))
        .body("path", equalTo("/api/test/protected"))
        .body("timestamp", notNullValue());
  }

  @Test
  @DisplayName("유효하지 않은 JWT 토큰으로 보호된 엔드포인트 접근시 401을 반환한다")
  void givenInvalidJwtToken_whenAccessProtectedEndpoint_thenUnauthorized() {
    // Given: 유효하지 않은 JWT 토큰
    String invalidToken = "invalid.jwt.token";

    // When & Then: 보호된 엔드포인트에 유효하지 않은 토큰으로 접근
    given()
        .header("Authorization", "Bearer " + invalidToken)
        .when()
        .get("/api/test/protected")
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(401))
        .body("error", equalTo("Unauthorized"));
  }

  @Test
  @DisplayName("JWT 토큰 없이 보호된 엔드포인트 접근시 401을 반환한다")
  void givenNoJwtToken_whenAccessProtectedEndpoint_thenUnauthorized() {
    // When & Then: 토큰 없이 보호된 엔드포인트에 접근
    given()
        .when()
        .get("/api/test/protected")
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(401))
        .body("error", equalTo("Unauthorized"))
        .body("path", equalTo("/api/test/protected"));
  }

  @Test
  @DisplayName("잘못된 Bearer 토큰 형식으로 접근시 401을 반환한다")
  void givenInvalidBearerFormat_whenAccessProtectedEndpoint_thenUnauthorized() {
    // Given: Bearer 형식이 아닌 토큰
    User user = createTestUser("format@example.com", "password123", "formatuser");
    String validToken =
        jwtTokenGenerator.generateAccessToken(
            user.getId(), user.getEmail(), user.getNickname(), user.getRole().name());

    // When & Then: Bearer 접두사 없이 접근
    given()
        .header("Authorization", validToken) // Bearer 접두사 없음
        .when()
        .get("/api/test/protected")
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  @DisplayName("유효한 JWT 토큰으로 사용자 정보 조회가 가능하다")
  void givenValidJwtToken_whenGetAuthInfo_thenReturnUserInfo() {
    // Given: 유효한 사용자와 JWT 토큰 생성
    User user = createTestUser("info@example.com", "password123", "infouser");
    String validToken =
        jwtTokenGenerator.generateAccessToken(
            user.getId(), user.getEmail(), user.getNickname(), user.getRole().name());

    // When & Then: 인증 정보 조회
    given()
        .header("Authorization", "Bearer " + validToken)
        .when()
        .get("/api/test/auth-info")
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("message", containsString("Authenticated user: " + user.getEmail()))
        .body("user", equalTo(user.getEmail()));
  }

  private User createTestUser(String email, String password, String nickname) {
    User user = User.register(email, nickname, passwordEncoder.encode(password));
    return userRepository.save(user);
  }
}
