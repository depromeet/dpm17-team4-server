package depromeet.lessonfour.server.auth.api;

import static io.restassured.RestAssured.given;
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

import depromeet.lessonfour.server.auth.domain.vo.AccountContext;
import depromeet.lessonfour.server.auth.infra.security.jwt.JwtTokenGenerator;
import depromeet.lessonfour.server.user.domain.entity.User;
import depromeet.lessonfour.server.user.infra.repository.UserRepository;
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
    String validToken = jwtTokenGenerator.generateAccessToken(AccountContext.of(user));

    // When & Then: Echo API에 유효한 토큰으로 접근
    given()
        .header("Authorization", "Bearer " + validToken)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("{\"Hello\": \"World!\"}")
        .when()
        .post("/api/v1/echo")
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(equalTo("{\"Hello\": \"World!\"}"));
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
            Instant.now().minus(1, ChronoUnit.HOURS)); // 1시간 전에 만료

    // When & Then: 보호된 엔드포인트에 만료된 토큰으로 접근
    given()
        .header("Authorization", "Bearer " + expiredToken)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("{\"test\": \"data\"}")
        .when()
        .post("/api/v1/echo")
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(401))
        .body("error", equalTo("Unauthorized"))
        .body("path", equalTo("/api/v1/echo"))
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
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("{\"test\": \"data\"}")
        .when()
        .post("/api/v1/echo")
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
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("{\"test\": \"data\"}")
        .when()
        .post("/api/v1/echo")
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(401))
        .body("error", equalTo("Unauthorized"))
        .body("path", equalTo("/api/v1/echo"));
  }

  @Test
  @DisplayName("잘못된 Bearer 토큰 형식으로 접근시 401을 반환한다")
  void givenInvalidBearerFormat_whenAccessProtectedEndpoint_thenUnauthorized() {
    // Given: Bearer 형식이 아닌 토큰
    User user = createTestUser("format@example.com", "password123", "formatuser");
    String validToken = jwtTokenGenerator.generateAccessToken(AccountContext.of(user));

    // When & Then: Bearer 접두사 없이 접근
    given()
        .header("Authorization", validToken) // Bearer 접두사 없음
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("{\"test\": \"data\"}")
        .when()
        .post("/api/v1/echo")
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  @DisplayName("유효한 JWT 토큰으로 Echo API에 두 번째 요청도 성공한다")
  void givenValidJwtToken_whenSecondEchoRequest_thenSuccess() {
    // Given: 유효한 사용자와 JWT 토큰 생성
    User user = createTestUser("info@example.com", "password123", "infouser");
    String validToken = jwtTokenGenerator.generateAccessToken(AccountContext.of(user));

    // When & Then: Echo API에 두 번째 요청
    given()
        .header("Authorization", "Bearer " + validToken)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("{\"second\": \"request\"}")
        .when()
        .post("/api/v1/echo")
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(equalTo("{\"second\": \"request\"}"));
  }

  private User createTestUser(String email, String password, String nickname) {
    User user = User.register(email, nickname, passwordEncoder.encode(password));
    return userRepository.save(user);
  }
}
