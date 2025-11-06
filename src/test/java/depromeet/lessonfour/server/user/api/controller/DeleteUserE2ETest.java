package depromeet.lessonfour.server.user.api.controller;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

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
import depromeet.lessonfour.server.user.domain.repository.UserRepository;
import io.restassured.RestAssured;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql(
    scripts = "/sql/cleanup.sql",
    config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED),
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class DeleteUserE2ETest {

  @LocalServerPort private int port;

  @Autowired private JwtTokenGenerator jwtTokenGenerator;
  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  private String validJwtToken;
  private User testUser;

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

    // 테스트용 사용자 생성
    testUser = createTestUser("test@example.com", "password123", "testuser");
    validJwtToken = "Bearer " + jwtTokenGenerator.generateAccessToken(AccountContext.of(testUser));
  }

  private User createTestUser(String email, String password, String nickname) {
    User user = User.register(email, nickname, passwordEncoder.encode(password));
    return userRepository.save(user);
  }

  @Test
  @DisplayName("유효한 사용자 계정 삭제 요청시 성공적으로 삭제된다")
  void givenValidUserDeleteRequest_whenDeleteUser_thenSuccess() {
    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .when()
        .delete("/api/v1/users/me")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("message", equalTo("삭제가 완료되었습니다."));
  }

  @Test
  @DisplayName("JWT 토큰 없이 삭제 요청시 401 에러를 반환한다")
  void givenNoAuthToken_whenDeleteUser_thenUnauthorized() {
    given()
        .log()
        .all()
        .when()
        .delete("/api/v1/users/me")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  @DisplayName("유효하지 않은 JWT 토큰으로 삭제 요청시 401 에러를 반환한다")
  void givenInvalidAuthToken_whenDeleteUser_thenUnauthorized() {
    given()
        .log()
        .all()
        .header("Authorization", "Bearer invalid-token")
        .when()
        .delete("/api/v1/users/me")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  @DisplayName("삭제 후 소프트 삭제 상태를 확인한다")
  void givenSuccessfulDelete_whenCheckUser_thenSoftDeleted() {
    // 삭제 요청 수행
    given()
        .header("Authorization", validJwtToken)
        .when()
        .delete("/api/v1/users/me")
        .then()
        .statusCode(HttpStatus.OK.value());

    // 데이터베이스에서 삭제 상태 확인
    User deletedUser =
        userRepository
            .findById(testUser.getId())
            .orElseThrow(() -> new RuntimeException("User should still exist in database"));

    assertThat(deletedUser.isDeleted()).isTrue();
  }

  @Test
  @DisplayName("이미 삭제된 사용자 계정 삭제 요청시 401 에러가 발생한다")
  void givenAlreadyDeletedUser_whenDeleteUser_thenSuccessIdempotently() {
    // 먼저 사용자를 삭제
    testUser.deactivate();
    userRepository.save(testUser);

    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .when()
        .delete("/api/v1/users/me")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.UNAUTHORIZED.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE);
  }

  @Test
  @DisplayName("잘못된 Bearer 토큰 형식으로 요청시 401 에러를 반환한다")
  void givenInvalidBearerFormat_whenDeleteUser_thenUnauthorized() {
    String tokenWithoutBearer = validJwtToken.replace("Bearer ", "");

    given()
        .log()
        .all()
        .header("Authorization", tokenWithoutBearer) // Bearer 접두사 없음
        .when()
        .delete("/api/v1/users/me")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  @DisplayName("여러 사용자 중 특정 사용자만 삭제된다")
  void givenMultipleUsers_whenDeleteOne_thenOnlyTargetDeleted() {
    // 추가 사용자 생성
    User anotherUser = createTestUser("another@example.com", "password456", "anotheruser");
    userRepository.save(anotherUser);

    // 첫 번째 사용자 삭제
    given()
        .header("Authorization", validJwtToken)
        .when()
        .delete("/api/v1/users/me")
        .then()
        .statusCode(HttpStatus.OK.value());

    // 첫 번째 사용자는 삭제 상태, 두 번째 사용자는 유지 상태 확인
    User firstUser = userRepository.findById(testUser.getId()).orElseThrow();
    User secondUser = userRepository.findById(anotherUser.getId()).orElseThrow();

    assertThat(firstUser.isDeleted()).isTrue();
    assertThat(secondUser.isDeleted()).isFalse();
  }
}
