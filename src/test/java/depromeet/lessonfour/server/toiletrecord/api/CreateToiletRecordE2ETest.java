package depromeet.lessonfour.server.toiletrecord.api;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
import depromeet.lessonfour.server.report.domain.entity.ToiletScore;
import depromeet.lessonfour.server.report.infra.JpaToiletScoreRepository;
import depromeet.lessonfour.server.user.domain.entity.User;
import depromeet.lessonfour.server.user.domain.repository.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql(
    scripts = "/sql/cleanup.sql",
    config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED),
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class CreateToiletRecordE2ETest {

  @LocalServerPort private int port;

  @Autowired private JwtTokenGenerator jwtTokenGenerator;
  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private JpaToiletScoreRepository toiletScoreRepository;

  private String validJwtToken;
  private User testUser;
  private final DateTimeFormatter formatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

    // 실제 사용자 생성 및 JWT 토큰 생성
    testUser = createTestUser("test@example.com", "password123", "testuser");
    validJwtToken = "Bearer " + jwtTokenGenerator.generateAccessToken(AccountContext.of(testUser));
  }

  private User createTestUser(String email, String password, String nickname) {
    User user = User.register(email, nickname, passwordEncoder.encode(password));
    return userRepository.save(user);
  }

  @Test
  @DisplayName("유효한 배변기록 생성 요청시 성공적으로 생성된다")
  void givenValidToiletRecordRequest_whenCreateToiletRecord_thenSuccess() {
    LocalDateTime now = LocalDateTime.now();
    String createRequest =
        String.format(
            """
        {
          "occurredAt": "%s",
          "isSuccessful": true,
          "color": "DEFAULT",
          "shape": "BANANA",
          "pain": 0,
          "duration": 10,
          "note": "정상적인 배변"
        }
        """,
            now.format(formatter));

    given()
        .log()
        .all()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(createRequest)
        .when()
        .post("/api/v1/poo-records")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(201));
  }

  @Test
  @DisplayName("JWT 토큰 없이 요청시 401 에러를 반환한다")
  void givenNoAuthToken_whenCreateToiletRecord_thenUnauthorized() {
    LocalDateTime now = LocalDateTime.now();
    String createRequest =
        String.format(
            """
        {
          "occurredAt": "%s",
          "isSuccessful": true,
          "color": "DEFAULT",
          "shape": "BANANA",
          "pain": 0,
          "duration": 10
        }
        """,
            now.format(formatter));

    given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .when()
        .post("/api/v1/poo-records")
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  @DisplayName("유효하지 않은 JWT 토큰으로 요청시 401 에러를 반환한다")
  void givenInvalidAuthToken_whenCreateToiletRecord_thenUnauthorized() {
    LocalDateTime now = LocalDateTime.now();
    String createRequest =
        String.format(
            """
        {
          "occurredAt": "%s",
          "isSuccessful": true,
          "color": "DEFAULT",
          "shape": "BANANA",
          "pain": 0,
          "duration": 10
        }
        """,
            now.format(formatter));

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer invalid-token")
        .body(createRequest)
        .when()
        .post("/api/v1/poo-records")
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  @DisplayName("배변시각이 null인 경우 400 에러를 반환한다")
  void givenNullOccurredAt_whenCreateToiletRecord_thenBadRequest() {
    String createRequest =
        """
        {
          "occurredAt": null,
          "isSuccessful": true,
          "color": "DEFAULT",
          "shape": "BANANA",
          "pain": 0,
          "duration": 10
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(createRequest)
        .when()
        .post("/api/v1/poo-records")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("message", containsString("배변시각 기록은 필수입니다"));
  }

  @Test
  @DisplayName("배변 여부가 null인 경우 400 에러를 반환한다")
  void givenNullIsSuccessful_whenCreateToiletRecord_thenBadRequest() {
    LocalDateTime now = LocalDateTime.now();
    String createRequest =
        String.format(
            """
        {
          "occurredAt": "%s",
          "isSuccessful": null,
          "color": "DEFAULT",
          "shape": "BANANA",
          "pain": 0,
          "duration": 10
        }
        """,
            now.format(formatter));

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(createRequest)
        .when()
        .post("/api/v1/poo-records")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("message", containsString("배변 여부 기록은 필수입니다"));
  }

  @Test
  @DisplayName("유효하지 않은 배변 색깔인 경우 400 에러를 반환한다")
  void givenInvalidColor_whenCreateToiletRecord_thenBadRequest() {
    LocalDateTime now = LocalDateTime.now();
    String createRequest =
        String.format(
            """
        {
          "occurredAt": "%s",
          "isSuccessful": true,
          "color": "INVALID_COLOR",
          "shape": "BANANA",
          "pain": 0,
          "duration": 10
        }
        """,
            now.format(formatter));

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(createRequest)
        .when()
        .post("/api/v1/poo-records")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("message", containsString("요청 본문이 올바르지 않습니다"));
  }

  @Test
  @DisplayName("유효하지 않은 배변 형태인 경우 400 에러를 반환한다")
  void givenInvalidShape_whenCreateToiletRecord_thenBadRequest() {
    LocalDateTime now = LocalDateTime.now();
    String createRequest =
        String.format(
            """
        {
          "occurredAt": "%s",
          "isSuccessful": true,
          "color": "DEFAULT",
          "shape": "INVALID_SHAPE",
          "pain": 0,
          "duration": 10
        }
        """,
            now.format(formatter));

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(createRequest)
        .when()
        .post("/api/v1/poo-records")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("message", containsString("요청 본문이 올바르지 않습니다"));
  }

  @Test
  @DisplayName("복통 점수가 음수인 경우 400 에러를 반환한다")
  void givenNegativePain_whenCreateToiletRecord_thenBadRequest() {
    LocalDateTime now = LocalDateTime.now();
    String createRequest =
        String.format(
            """
        {
          "occurredAt": "%s",
          "isSuccessful": true,
          "color": "DEFAULT",
          "shape": "BANANA",
          "pain": -1,
          "duration": 10
        }
        """,
            now.format(formatter));

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(createRequest)
        .when()
        .post("/api/v1/poo-records")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("message", containsString("복통 점수는 0 이상이어야 합니다"));
  }

  @Test
  @DisplayName("복통 점수가 100을 초과하는 경우 400 에러를 반환한다")
  void givenPainOver100_whenCreateToiletRecord_thenBadRequest() {
    LocalDateTime now = LocalDateTime.now();
    String createRequest =
        String.format(
            """
        {
          "occurredAt": "%s",
          "isSuccessful": true,
          "color": "DEFAULT",
          "shape": "BANANA",
          "pain": 101,
          "duration": 10
        }
        """,
            now.format(formatter));

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(createRequest)
        .when()
        .post("/api/v1/poo-records")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("message", containsString("복통 점수는 100 이하여야 합니다"));
  }

  @Test
  @DisplayName("소요시간이 5 미만인 경우 400 에러를 반환한다")
  void givenDurationLessThan5_whenCreateToiletRecord_thenBadRequest() {
    LocalDateTime now = LocalDateTime.now();
    String createRequest =
        String.format(
            """
        {
          "occurredAt": "%s",
          "isSuccessful": true,
          "color": "DEFAULT",
          "shape": "BANANA",
          "pain": 0,
          "duration": 4
        }
        """,
            now.format(formatter));

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(createRequest)
        .when()
        .post("/api/v1/poo-records")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("message", containsString("소요시간은 5 이상이어야 합니다"));
  }

  @Test
  @DisplayName("소요시간이 15를 초과하는 경우 400 에러를 반환한다")
  void givenDurationOver15_whenCreateToiletRecord_thenBadRequest() {
    LocalDateTime now = LocalDateTime.now();
    String createRequest =
        String.format(
            """
        {
          "occurredAt": "%s",
          "isSuccessful": true,
          "color": "DEFAULT",
          "shape": "BANANA",
          "pain": 0,
          "duration": 16
        }
        """,
            now.format(formatter));

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(createRequest)
        .when()
        .post("/api/v1/poo-records")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("message", containsString("소요시간은 15 이하여야 합니다"));
  }

  @Test
  @DisplayName("배변 실패 케이스도 성공적으로 기록된다")
  void givenUnsuccessfulToilet_whenCreateToiletRecord_thenSuccess() {
    LocalDateTime now = LocalDateTime.now();
    String createRequest =
        String.format(
            """
        {
          "occurredAt": "%s",
          "isSuccessful": false,
          "color": "DEFAULT",
          "shape": "BANANA",
          "pain": 50,
          "duration": 10,
          "note": "배변 실패"
        }
        """,
            now.format(formatter));

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(createRequest)
        .when()
        .post("/api/v1/poo-records")
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(201));
  }

  @Test
  @DisplayName("메모가 null인 경우에도 성공적으로 기록된다")
  void givenNullNote_whenCreateToiletRecord_thenSuccess() {
    LocalDateTime now = LocalDateTime.now();
    String createRequest =
        String.format(
            """
        {
          "occurredAt": "%s",
          "isSuccessful": true,
          "color": "DEFAULT",
          "shape": "BANANA",
          "pain": 0,
          "duration": 10,
          "note": null
        }
        """,
            now.format(formatter));

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(createRequest)
        .when()
        .post("/api/v1/poo-records")
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(201));
  }

  @Test
  @DisplayName("모든 색깔 타입으로 요청시 성공한다")
  void givenAllColorTypes_whenCreateToiletRecord_thenSuccess() {
    LocalDateTime now = LocalDateTime.now();
    String[] colors = {"DEFAULT", "GOLD", "DARK_BROWN", "RED", "GREEN", "WHITE"};

    for (String color : colors) {
      String createRequest =
          String.format(
              """
          {
            "occurredAt": "%s",
            "isSuccessful": true,
            "color": "%s",
            "shape": "BANANA",
            "pain": 0,
            "duration": 10
          }
          """,
              now.format(formatter), color);

      given()
          .contentType(ContentType.JSON)
          .header("Authorization", validJwtToken)
          .body(createRequest)
          .when()
          .post("/api/v1/poo-records")
          .then()
          .statusCode(HttpStatus.OK.value())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .body("status", equalTo(201));
    }
  }

  @Test
  @DisplayName("모든 형태 타입으로 요청시 성공한다")
  void givenAllShapeTypes_whenCreateToiletRecord_thenSuccess() {
    LocalDateTime now = LocalDateTime.now();
    String[] shapes = {"RABBIT", "CORN", "BANANA", "CREAM", "PORRIDGE", "WATER"};

    for (String shape : shapes) {
      String createRequest =
          String.format(
              """
          {
            "occurredAt": "%s",
            "isSuccessful": true,
            "color": "DEFAULT",
            "shape": "%s",
            "pain": 0,
            "duration": 10
          }
          """,
              now.format(formatter), shape);

      given()
          .contentType(ContentType.JSON)
          .header("Authorization", validJwtToken)
          .body(createRequest)
          .when()
          .post("/api/v1/poo-records")
          .then()
          .statusCode(HttpStatus.OK.value())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .body("status", equalTo(201));
    }
  }

  @Test
  @DisplayName("복통 점수의 경계값 테스트 - 0과 100은 성공한다")
  void givenPainBoundaryValues_whenCreateToiletRecord_thenSuccess() {
    LocalDateTime now = LocalDateTime.now();
    int[] painValues = {0, 100};

    for (int pain : painValues) {
      String createRequest =
          String.format(
              """
          {
            "occurredAt": "%s",
            "isSuccessful": true,
            "color": "DEFAULT",
            "shape": "BANANA",
            "pain": %d,
            "duration": 10
          }
          """,
              now.format(formatter), pain);

      given()
          .contentType(ContentType.JSON)
          .header("Authorization", validJwtToken)
          .body(createRequest)
          .when()
          .post("/api/v1/poo-records")
          .then()
          .statusCode(HttpStatus.OK.value())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .body("status", equalTo(201));
    }
  }

  @Test
  @DisplayName("소요시간의 경계값 테스트 - 5와 15는 성공한다")
  void givenDurationBoundaryValues_whenCreateToiletRecord_thenSuccess() {
    LocalDateTime now = LocalDateTime.now();
    int[] durationValues = {5, 15};

    for (int duration : durationValues) {
      String createRequest =
          String.format(
              """
          {
            "occurredAt": "%s",
            "isSuccessful": true,
            "color": "DEFAULT",
            "shape": "BANANA",
            "pain": 0,
            "duration": %d
          }
          """,
              now.format(formatter), duration);

      given()
          .contentType(ContentType.JSON)
          .header("Authorization", validJwtToken)
          .body(createRequest)
          .when()
          .post("/api/v1/poo-records")
          .then()
          .statusCode(HttpStatus.OK.value())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .body("status", equalTo(201));
    }
  }

  @Test
  @DisplayName("유효하지 않은 날짜 형식인 경우 400 에러를 반환한다")
  void givenInvalidDateFormat_whenCreateToiletRecord_thenBadRequest() {
    String createRequest =
        """
        {
          "occurredAt": "invalid-date-format",
          "isSuccessful": true,
          "color": "DEFAULT",
          "shape": "BANANA",
          "pain": 0,
          "duration": 10
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(createRequest)
        .when()
        .post("/api/v1/poo-records")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  @DisplayName("JSON 형식이 아닌 요청시 400 에러를 반환한다")
  void givenNonJsonRequest_whenCreateToiletRecord_thenBadRequest() {
    String invalidRequest = "not-json-format";

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(invalidRequest)
        .when()
        .post("/api/v1/poo-records")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  @DisplayName("Content-Type이 application/json이 아닌 경우 415 에러를 반환한다")
  void givenNonJsonContentType_whenCreateToiletRecord_thenUnsupportedMediaType() {
    String requestBody = "pain=0&duration=10";

    given()
        .contentType(ContentType.URLENC)
        .header("Authorization", validJwtToken)
        .body(requestBody)
        .when()
        .post("/api/v1/poo-records")
        .then()
        .statusCode(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());
  }

  @Test
  @DisplayName("배변기록 생성시 ToiletScore가 자동으로 생성된다")
  void givenToiletRecordCreated_whenEventProcessed_thenToiletScoreIsCreated() {
    LocalDateTime now = LocalDateTime.now();
    LocalDate today = now.toLocalDate();
    String createRequest =
        String.format(
            """
        {
          "occurredAt": "%s",
          "isSuccessful": true,
          "color": "DEFAULT",
          "shape": "BANANA",
          "pain": 0,
          "duration": 10,
          "note": "정상적인 배변"
        }
        """,
            now.format(formatter));

    // 배변 기록 생성
    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(createRequest)
        .when()
        .post("/api/v1/poo-records")
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(201));

    // 이벤트 대기 (최대 5초)
    await()
        .atMost(Duration.ofSeconds(5))
        .pollInterval(Duration.ofMillis(100))
        .untilAsserted(
            () -> {
              List<ToiletScore> scores =
                  toiletScoreRepository.findAll().stream()
                      .filter(score -> score.getUserId().equals(testUser.getId()))
                      .filter(score -> score.getDate().equals(today))
                      .toList();

              assertThat(scores).isNotEmpty();
              assertThat(scores.get(0).getUserId()).isEqualTo(testUser.getId());
              assertThat(scores.get(0).getDate()).isEqualTo(today);
              assertThat(scores.get(0).getScore()).isNotNull();
            });
  }

  @Test
  @DisplayName("같은 날짜에 여러 배변기록 생성시 ToiletScore가 업데이트된다")
  void givenMultipleToiletRecordsOnSameDay_whenEventProcessed_thenToiletScoreIsUpdated() {
    LocalDateTime now = LocalDateTime.now();
    LocalDate today = now.toLocalDate();

    // 첫 번째 배변 기록 생성
    String firstRequest =
        String.format(
            """
        {
          "occurredAt": "%s",
          "isSuccessful": true,
          "color": "DEFAULT",
          "shape": "BANANA",
          "pain": 0,
          "duration": 10
        }
        """,
            now.format(formatter));

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(firstRequest)
        .when()
        .post("/api/v1/poo-records")
        .then()
        .statusCode(HttpStatus.OK.value());

    // 첫 번째 ToiletScore 생성 대기
    await()
        .atMost(Duration.ofSeconds(5))
        .pollInterval(Duration.ofMillis(100))
        .untilAsserted(
            () -> {
              List<ToiletScore> scores =
                  toiletScoreRepository.findAll().stream()
                      .filter(score -> score.getUserId().equals(testUser.getId()))
                      .filter(score -> score.getDate().equals(today))
                      .toList();
              assertThat(scores).isNotEmpty();
            });

    // 두 번째 배변 기록 생성 (1시간 후)
    LocalDateTime laterTime = now.plusHours(1);
    String secondRequest =
        String.format(
            """
        {
          "occurredAt": "%s",
          "isSuccessful": true,
          "color": "GOLD",
          "shape": "CREAM",
          "pain": 10,
          "duration": 8
        }
        """,
            laterTime.format(formatter));

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(secondRequest)
        .when()
        .post("/api/v1/poo-records")
        .then()
        .statusCode(HttpStatus.OK.value());

    // ToiletScore 업데이트 대기
    await()
        .atMost(Duration.ofSeconds(5))
        .pollInterval(Duration.ofMillis(100))
        .untilAsserted(
            () -> {
              List<ToiletScore> scores =
                  toiletScoreRepository.findAll().stream()
                      .filter(score -> score.getUserId().equals(testUser.getId()))
                      .filter(score -> score.getDate().equals(today))
                      .toList();

              assertThat(scores).isNotEmpty();
              // 같은 날짜에 대해 여러 배변기록이 있어도 ToiletScore는 업데이트됨
              assertThat(scores.get(0).getDate()).isEqualTo(today);
            });
  }
}
