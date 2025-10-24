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
import depromeet.lessonfour.server.user.domain.vo.Gender;
import io.restassured.RestAssured;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql(
    scripts = "/sql/cleanup.sql",
    config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED),
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ProfileUpdateE2ETest {

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
  @DisplayName("유효한 프로필 업데이트 요청시 성공적으로 업데이트된다")
  void givenValidProfileUpdateRequest_whenUpdateProfile_thenSuccess() {
    String requestBody =
        """
        {
          "nickname": "updateduser",
          "profileImage": "https://example.com/new-profile.jpg",
          "gender": "M",
          "birthYear": 1990
        }
        """;

    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(requestBody)
        .when()
        .patch("/api/v1/users/me")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.nickname", equalTo("updateduser"))
        .body("data.profileImage", equalTo("https://example.com/new-profile.jpg"))
        .body("data.gender", equalTo("M"))
        .body("data.birthYear", equalTo(1990));
  }

  @Test
  @DisplayName("JWT 토큰 없이 프로필 업데이트 요청시 401 에러를 반환한다")
  void givenNoAuthToken_whenUpdateProfile_thenUnauthorized() {
    String requestBody = """
        {
          "nickname": "updateduser"
        }
        """;

    given()
        .log()
        .all()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(requestBody)
        .when()
        .patch("/api/v1/users/me")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  @DisplayName("유효하지 않은 JWT 토큰으로 프로필 업데이트 요청시 401 에러를 반환한다")
  void givenInvalidAuthToken_whenUpdateProfile_thenUnauthorized() {
    String requestBody = """
        {
          "nickname": "updateduser"
        }
        """;

    given()
        .log()
        .all()
        .header("Authorization", "Bearer invalid-token")
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(requestBody)
        .when()
        .patch("/api/v1/users/me")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  @DisplayName("닉네임만 업데이트할 수 있다")
  void givenOnlyNickname_whenUpdateProfile_thenSuccess() {
    String requestBody = """
        {
          "nickname": "newnickname"
        }
        """;

    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(requestBody)
        .when()
        .patch("/api/v1/users/me")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.nickname", equalTo("newnickname"));
  }

  @Test
  @DisplayName("프로필 이미지만 업데이트할 수 있다")
  void givenOnlyProfileImage_whenUpdateProfile_thenSuccess() {
    String requestBody =
        """
        {
          "profileImage": "https://example.com/profile.jpg"
        }
        """;

    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(requestBody)
        .when()
        .patch("/api/v1/users/me")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.profileImage", equalTo("https://example.com/profile.jpg"));
  }

  @Test
  @DisplayName("성별만 업데이트할 수 있다")
  void givenOnlyGender_whenUpdateProfile_thenSuccess() {
    String requestBody = """
        {
          "gender": "F"
        }
        """;

    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(requestBody)
        .when()
        .patch("/api/v1/users/me")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.gender", equalTo("F"));
  }

  @Test
  @DisplayName("출생년도만 업데이트할 수 있다")
  void givenOnlyBirthYear_whenUpdateProfile_thenSuccess() {
    String requestBody = """
        {
          "birthYear": 1985
        }
        """;

    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(requestBody)
        .when()
        .patch("/api/v1/users/me")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.birthYear", equalTo(1985));
  }

  @Test
  @DisplayName("닉네임이 32자를 초과하면 400 에러를 반환한다")
  void givenNicknameTooLong_whenUpdateProfile_thenBadRequest() {
    String longNickname = "a".repeat(33);
    String requestBody =
        String.format(
            """
            {
              "nickname": "%s"
            }
            """,
            longNickname);

    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(requestBody)
        .when()
        .patch("/api/v1/users/me")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  @DisplayName("프로필 이미지 URL이 512자를 초과하면 400 에러를 반환한다")
  void givenProfileImageUrlTooLong_whenUpdateProfile_thenBadRequest() {
    String longUrl = "https://example.com/" + "a".repeat(500);
    String requestBody =
        String.format(
            """
            {
              "profileImage": "%s"
            }
            """,
            longUrl);

    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(requestBody)
        .when()
        .patch("/api/v1/users/me")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  @DisplayName("잘못된 성별 값으로 요청시 400 에러를 반환한다")
  void givenInvalidGender_whenUpdateProfile_thenBadRequest() {
    String requestBody = """
        {
          "gender": "INVALID"
        }
        """;

    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(requestBody)
        .when()
        .patch("/api/v1/users/me")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  @DisplayName("잘못된 출생년도로 요청시 400 에러를 반환한다")
  void givenInvalidBirthYear_whenUpdateProfile_thenBadRequest() {
    String requestBody = """
        {
          "birthYear": 1800
        }
        """;

    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(requestBody)
        .when()
        .patch("/api/v1/users/me")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  @DisplayName("빈 요청 본문으로 요청시 성공한다")
  void givenEmptyRequestBody_whenUpdateProfile_thenSuccess() {
    String requestBody = "{}";

    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(requestBody)
        .when()
        .patch("/api/v1/users/me")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200));
  }

  @Test
  @DisplayName("프로필 업데이트 후 데이터베이스에서 변경사항이 반영된다")
  void givenSuccessfulUpdate_whenCheckDatabase_thenUpdated() {
    String requestBody =
        """
        {
          "nickname": "updateduser",
          "gender": "F",
          "birthYear": 1995
        }
        """;

    // 프로필 업데이트 요청
    given()
        .header("Authorization", validJwtToken)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(requestBody)
        .when()
        .patch("/api/v1/users/me")
        .then()
        .statusCode(HttpStatus.OK.value());

    // 데이터베이스에서 변경사항 확인
    User updatedUser =
        userRepository
            .findById(testUser.getId())
            .orElseThrow(() -> new RuntimeException("User should exist in database"));

    assertThat(updatedUser.getNickname()).isEqualTo("updateduser");
    assertThat(updatedUser.getGender()).isEqualTo(Gender.F);
    assertThat(updatedUser.getBirthYear()).isEqualTo(1995);
  }

  @Test
  @DisplayName("잘못된 Bearer 토큰 형식으로 요청시 401 에러를 반환한다")
  void givenInvalidBearerFormat_whenUpdateProfile_thenUnauthorized() {
    String tokenWithoutBearer = validJwtToken.replace("Bearer ", "");
    String requestBody = """
        {
          "nickname": "updateduser"
        }
        """;

    given()
        .log()
        .all()
        .header("Authorization", tokenWithoutBearer) // Bearer 접두사 없음
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(requestBody)
        .when()
        .patch("/api/v1/users/me")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }
}
