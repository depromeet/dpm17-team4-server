package depromeet.lessonfour.server.term.api.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

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
class TermsControllerE2ETest {

  @LocalServerPort int port;

  @Autowired JwtTokenGenerator jwtTokenGenerator;
  @Autowired UserRepository userRepository;
  @Autowired PasswordEncoder passwordEncoder;

  private String validJwtToken;
  private User testUser;

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

    testUser = createTestUser("terms-user@example.com", "pw1234!", "terms-user");
    validJwtToken = "Bearer " + jwtTokenGenerator.generateAccessToken(AccountContext.of(testUser));
  }

  private User createTestUser(String email, String password, String nickname) {
    User u = User.register(email, nickname, passwordEncoder.encode(password));
    return userRepository.save(u);
  }

  @Test
  @DisplayName("인증 후 약관 리스트를 조회하면 서비스이용약관/개인정보처리방침이 함께 반환된다")
  void givenAuth_whenGetTerms_thenReturnsBothDocs() {
    given()
        .header("Authorization", validJwtToken)
        .when()
        .get("/api/v1/terms")
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data", hasSize(2))
        .body("data.title", containsInAnyOrder("서비스이용약관", "개인정보처리방침"))
        .body(
            "data.find { it.title == '개인정보처리방침' }.content",
            allOf(
                containsString("개인정보처리방침"),
                anyOf(containsString("개인정보 보호법"), containsString("본 방침은"))))
        .body(
            "data.find { it.title == '서비스이용약관' }.content",
            anyOf(containsString("서비스"), containsString("이용약관")));
  }

  @Test
  @DisplayName("JWT 토큰 없이 요청 시 401")
  void givenNoToken_whenGetTerms_thenUnauthorized() {
    given().when().get("/api/v1/terms").then().statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  @DisplayName("잘못된 토큰이면 401")
  void givenInvalidToken_whenGetTerms_thenUnauthorized() {
    given()
        .header("Authorization", "Bearer invalid-token")
        .when()
        .get("/api/v1/terms")
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }
}
