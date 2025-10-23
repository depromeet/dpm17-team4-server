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
  @DisplayName("인증 후 약관 리스트를 조회하면 두 문서를 고정 순서로 반환한다 (서비스이용약관 → 개인정보처리방침)")
  void givenAuth_whenGetTerms_thenReturnsTwoDocsInFixedOrder() {
    given()
        .header("Authorization", validJwtToken)
        .when()
        .get("/api/v1/terms")
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        // 정확히 2개
        .body("data", hasSize(2))
        // 고정 순서 검증
        .body("data[0].title", equalTo("서비스이용약관"))
        .body("data[1].title", equalTo("개인정보처리방침"))
        // 본문 존재/샘플 키워드 확인 (너무 세게 묶지 않기)
        .body("data[0].content", allOf(not(isEmptyOrNullString()), containsString("서비스")))
        .body("data[1].content", allOf(not(isEmptyOrNullString()), containsString("개인정보처리방침")));
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
