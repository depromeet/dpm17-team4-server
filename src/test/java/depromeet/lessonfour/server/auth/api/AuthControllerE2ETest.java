package depromeet.lessonfour.server.auth.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql(
    scripts = "/sql/cleanup.sql",
    config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED),
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class AuthControllerE2ETest {

  @LocalServerPort private int port;

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
  }

  @Test
  @DisplayName("유효한 회원가입 요청시 성공적으로 회원가입된다")
  void givenValidRegisterRequest_whenRegister_thenSuccess() {
    String registerRequest =
        """
        {
          "email": "test@example.com",
          "password": "password123",
          "nickname": "testuser"
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(registerRequest)
        .when()
        .post("/api/auth/register")
        .then()
        .statusCode(HttpStatus.OK.value());
  }

  @Test
  @DisplayName("이메일이 누락된 경우 400 에러를 반환한다")
  void givenMissingEmail_whenRegister_thenBadRequest() {
    String registerRequest =
        """
        {
          "password": "password123",
          "nickname": "testuser"
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(registerRequest)
        .when()
        .post("/api/auth/register")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("message", containsString("이메일은 필수입니다"));
  }

  @Test
  @DisplayName("잘못된 이메일 형식인 경우 400 에러를 반환한다")
  void givenInvalidEmailFormat_whenRegister_thenBadRequest() {
    String registerRequest =
        """
        {
          "email": "invalid-email",
          "password": "password123",
          "nickname": "testuser"
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(registerRequest)
        .when()
        .post("/api/auth/register")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("message", containsString("올바른 이메일 형식이 아닙니다"));
  }

  @Test
  @DisplayName("비밀번호가 누락된 경우 400 에러를 반환한다")
  void givenMissingPassword_whenRegister_thenBadRequest() {
    String registerRequest =
        """
        {
          "email": "test@example.com",
          "nickname": "testuser"
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(registerRequest)
        .when()
        .post("/api/auth/register")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("message", containsString("비밀번호는 필수입니다"));
  }

  @Test
  @DisplayName("닉네임이 누락된 경우 400 에러를 반환한다")
  void givenMissingNickname_whenRegister_thenBadRequest() {
    String registerRequest =
        """
        {
          "email": "test@example.com",
          "password": "password123"
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(registerRequest)
        .when()
        .post("/api/auth/register")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("message", containsString("닉네임은 필수입니다"));
  }

  @Test
  @DisplayName("빈 이메일인 경우 400 에러를 반환한다")
  void givenEmptyEmail_whenRegister_thenBadRequest() {
    String registerRequest =
        """
        {
          "email": "",
          "password": "password123",
          "nickname": "testuser"
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(registerRequest)
        .when()
        .post("/api/auth/register")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("message", containsString("이메일은 필수입니다"));
  }

  @Test
  @DisplayName("빈 비밀번호인 경우 400 에러를 반환한다")
  void givenEmptyPassword_whenRegister_thenBadRequest() {
    String registerRequest =
        """
        {
          "email": "test@example.com",
          "password": "",
          "nickname": "testuser"
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(registerRequest)
        .when()
        .post("/api/auth/register")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("message", containsString("비밀번호는 필수입니다"));
  }

  @Test
  @DisplayName("빈 닉네임인 경우 400 에러를 반환한다")
  void givenEmptyNickname_whenRegister_thenBadRequest() {
    String registerRequest =
        """
        {
          "email": "test@example.com",
          "password": "password123",
          "nickname": ""
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(registerRequest)
        .when()
        .post("/api/auth/register")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("message", containsString("닉네임은 필수입니다"));
  }

  @Test
  @DisplayName("동일한 이메일로 중복 가입 시도시 에러를 반환한다")
  void givenDuplicateEmail_whenRegister_thenConflict() {
    String registerRequest =
        """
        {
          "email": "duplicate@example.com",
          "password": "password123",
          "nickname": "user1"
        }
        """;

    // 첫 번째 회원가입 성공
    given()
        .contentType(ContentType.JSON)
        .body(registerRequest)
        .when()
        .post("/api/auth/register")
        .then()
        .statusCode(HttpStatus.OK.value());

    // 같은 이메일로 두 번째 회원가입 시도
    String duplicateRequest =
        """
        {
          "email": "duplicate@example.com",
          "password": "password456",
          "nickname": "user2"
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(duplicateRequest)
        .when()
        .post("/api/auth/register")
        .then()
        .statusCode(HttpStatus.CONFLICT.value());
  }

  @Test
  @DisplayName("동일한 닉네임으로 중복 가입 시도시 에러를 반환한다")
  void givenDuplicateNickname_whenRegister_thenConflict() {
    String registerRequest =
        """
        {
          "email": "user1@example.com",
          "password": "password123",
          "nickname": "duplicatenick"
        }
        """;

    // 첫 번째 회원가입 성공
    given()
        .contentType(ContentType.JSON)
        .body(registerRequest)
        .when()
        .post("/api/auth/register")
        .then()
        .statusCode(HttpStatus.OK.value());

    // 같은 닉네임으로 두 번째 회원가입 시도
    String duplicateRequest =
        """
        {
          "email": "user2@example.com",
          "password": "password456",
          "nickname": "duplicatenick"
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(duplicateRequest)
        .when()
        .post("/api/auth/register")
        .then()
        .statusCode(HttpStatus.CONFLICT.value());
  }

  @Test
  @DisplayName("JSON 형식이 아닌 요청시 400 에러를 반환한다")
  void givenNonJsonRequest_whenRegister_thenBadRequest() {
    String invalidRequest = "not-json-format";

    given()
        .contentType(ContentType.JSON)
        .body(invalidRequest)
        .when()
        .post("/api/auth/register")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  @DisplayName("Content-Type이 application/json이 아닌 경우 415 에러를 반환한다")
  void givenNonJsonContentType_whenRegister_thenUnsupportedMediaType() {
    String registerRequest = "email=test@example.com&password=password123&nickname=testuser";

    given()
        .contentType(ContentType.URLENC)
        .body(registerRequest)
        .when()
        .post("/api/auth/register")
        .then()
        .statusCode(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());
  }
}
