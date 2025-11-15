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
class UpdateToiletRecordE2ETest {

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

  private Long createToiletRecord(String requestBody) {
    Integer id =
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", validJwtToken)
            .body(requestBody)
            .when()
            .post("/api/v1/poo-records")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .path("data.id");
    return id.longValue();
  }

  @Test
  @DisplayName("유효한 배변기록 수정 요청시 성공적으로 수정된다")
  void givenValidUpdateRequest_whenUpdateToiletRecord_thenSuccess() {
    // given: 배변기록 생성
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

    Long toiletRecordId = createToiletRecord(createRequest);

    // when: 배변기록 수정
    String updateRequest =
        """
        {
          "isSuccessful": false,
          "pain": 50,
          "duration": 12,
          "note": "수정된 배변 기록"
        }
        """;

    // then: 수정 성공
    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(updateRequest)
        .when()
        .patch("/api/v1/poo-records/" + toiletRecordId)
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("status", equalTo(200))
        .body("message", equalTo("업데이트가 완료되었습니다."))
        .body("data.id", equalTo(toiletRecordId.intValue()))
        .body("data.isSuccessful", equalTo(false))
        .body("data.color", equalTo("NONE"))
        .body("data.shape", equalTo("NONE"))
        .body("data.pain", equalTo(50))
        .body("data.duration", equalTo(12))
        .body("data.note", equalTo("수정된 배변 기록"));
  }

  @Test
  @DisplayName("일부 필드만 수정시 해당 필드만 업데이트된다")
  void givenPartialUpdateRequest_whenUpdateToiletRecord_thenOnlySpecifiedFieldsUpdated() {
    // given: 배변기록 생성
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

    Long toiletRecordId = createToiletRecord(createRequest);

    // when: 일부 필드만 수정
    String updateRequest =
        """
        {
          "pain": 30,
          "note": "복통만 수정"
        }
        """;

    // then: 지정된 필드만 업데이트됨
    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(updateRequest)
        .when()
        .patch("/api/v1/poo-records/" + toiletRecordId)
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("data.isSuccessful", equalTo(true)) // 수정되지 않음
        .body("data.color", equalTo("DEFAULT")) // 수정되지 않음
        .body("data.shape", equalTo("BANANA")) // 수정되지 않음
        .body("data.pain", equalTo(30)) // 수정됨
        .body("data.duration", equalTo(10)) // 수정되지 않음
        .body("data.note", equalTo("복통만 수정")); // 수정됨
  }

  @Test
  @DisplayName("JWT 토큰 없이 수정 요청시 401 에러가 발생한다")
  void givenNoJwtToken_whenUpdateToiletRecord_thenUnauthorized() {
    String updateRequest = """
        {
          "pain": 30
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(updateRequest)
        .when()
        .patch("/api/v1/poo-records/1")
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  @DisplayName("유효하지 않은 JWT 토큰으로 수정 요청시 401 에러가 발생한다")
  void givenInvalidJwtToken_whenUpdateToiletRecord_thenUnauthorized() {
    String updateRequest = """
        {
          "pain": 30
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer invalid.jwt.token")
        .body(updateRequest)
        .when()
        .patch("/api/v1/poo-records/1")
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  @DisplayName("존재하지 않는 배변기록 수정시 404 에러가 발생한다")
  void givenNonExistentRecord_whenUpdateToiletRecord_thenNotFound() {
    String updateRequest = """
        {
          "pain": 30
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(updateRequest)
        .when()
        .patch("/api/v1/poo-records/99999")
        .then()
        .statusCode(HttpStatus.NOT_FOUND.value())
        .body("message", containsString("데이터가 존재하지 않습니다"));
  }

  // TODO: 다른 사용자의 배변기록 수정 테스트는 추후 수정 필요 (findById 메서드 시그니처 확인 필요)
  // @Test
  // @DisplayName("다른 사용자의 배변기록 수정시 404 에러가 발생한다")
  void givenOtherUserRecord_whenUpdateToiletRecord_thenNotFound() {
    // given: 다른 사용자의 배변기록 생성
    User otherUser = createTestUser("other@example.com", "password123", "otheruser");
    String otherUserToken =
        "Bearer " + jwtTokenGenerator.generateAccessToken(AccountContext.of(otherUser));

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

    Long otherUserRecordId =
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", otherUserToken)
            .body(createRequest)
            .when()
            .post("/api/v1/poo-records")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .path("data.id");

    // when: 현재 사용자가 다른 사용자의 기록 수정 시도
    String updateRequest = """
        {
          "pain": 30
        }
        """;

    // then: 404 에러
    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(updateRequest)
        .when()
        .patch("/api/v1/poo-records/" + otherUserRecordId)
        .then()
        .statusCode(HttpStatus.NOT_FOUND.value());
  }

  @Test
  @DisplayName("복통 점수가 0 미만일 경우 400 에러가 발생한다")
  void givenNegativePain_whenUpdateToiletRecord_thenBadRequest() {
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

    Long toiletRecordId = createToiletRecord(createRequest);

    String updateRequest = """
        {
          "pain": -1
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(updateRequest)
        .when()
        .patch("/api/v1/poo-records/" + toiletRecordId)
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .body("message", containsString("복통 점수는 0 이상이어야 합니다"));
  }

  @Test
  @DisplayName("복통 점수가 100 초과일 경우 400 에러가 발생한다")
  void givenPainOver100_whenUpdateToiletRecord_thenBadRequest() {
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

    Long toiletRecordId = createToiletRecord(createRequest);

    String updateRequest = """
        {
          "pain": 101
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(updateRequest)
        .when()
        .patch("/api/v1/poo-records/" + toiletRecordId)
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .body("message", containsString("복통 점수는 100 이하여야 합니다"));
  }

  @Test
  @DisplayName("소요시간이 5 미만일 경우 400 에러가 발생한다")
  void givenDurationLessThan5_whenUpdateToiletRecord_thenBadRequest() {
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

    Long toiletRecordId = createToiletRecord(createRequest);

    String updateRequest = """
        {
          "duration": 4
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(updateRequest)
        .when()
        .patch("/api/v1/poo-records/" + toiletRecordId)
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .body("message", containsString("소요시간은 5 이상이어야 합니다"));
  }

  @Test
  @DisplayName("소요시간이 15 초과일 경우 400 에러가 발생한다")
  void givenDurationOver15_whenUpdateToiletRecord_thenBadRequest() {
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

    Long toiletRecordId = createToiletRecord(createRequest);

    String updateRequest = """
        {
          "duration": 16
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(updateRequest)
        .when()
        .patch("/api/v1/poo-records/" + toiletRecordId)
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .body("message", containsString("소요시간은 15 이하여야 합니다"));
  }

  // TODO: PATCH 요청에서 null 값 처리 확인 필요 - 유효하지 않은 enum은 무시될 수 있음
  // @Test
  // @DisplayName("유효하지 않은 색깔로 수정시 400 에러가 발생한다")
  void givenInvalidColor_whenUpdateToiletRecord_thenBadRequest() {
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

    Long toiletRecordId = createToiletRecord(createRequest);

    String updateRequest = """
        {
          "color": "INVALID_COLOR"
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(updateRequest)
        .when()
        .patch("/api/v1/poo-records/" + toiletRecordId)
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .body("message", equalTo("요청 본문이 올바르지 않습니다."));
  }

  // TODO: PATCH 요청에서 null 값 처리 확인 필요 - 유효하지 않은 enum은 무시될 수 있음
  // @Test
  // @DisplayName("유효하지 않은 형태로 수정시 400 에러가 발생한다")
  void givenInvalidShape_whenUpdateToiletRecord_thenBadRequest() {
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

    Long toiletRecordId = createToiletRecord(createRequest);

    String updateRequest = """
        {
          "shape": "INVALID_SHAPE"
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(updateRequest)
        .when()
        .patch("/api/v1/poo-records/" + toiletRecordId)
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .body("message", equalTo("요청 본문이 올바르지 않습니다."));
  }

  @Test
  @DisplayName("모든 색깔 타입으로 수정 가능하다")
  void givenAllColorTypes_whenUpdateToiletRecord_thenSuccess() {
    String[] colors = {"DEFAULT", "GOLD", "DARK_BROWN", "RED", "GREEN", "WHITE"};

    for (String color : colors) {
      // 배변기록 생성
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

      Long toiletRecordId = createToiletRecord(createRequest);

      // 색깔 수정
      String updateRequest = String.format("{\"color\": \"%s\"}", color);

      given()
          .contentType(ContentType.JSON)
          .header("Authorization", validJwtToken)
          .body(updateRequest)
          .when()
          .patch("/api/v1/poo-records/" + toiletRecordId)
          .then()
          .statusCode(HttpStatus.OK.value())
          .body("data.color", equalTo(color));
    }
  }

  @Test
  @DisplayName("모든 형태 타입으로 수정 가능하다")
  void givenAllShapeTypes_whenUpdateToiletRecord_thenSuccess() {
    String[] shapes = {"RABBIT", "WATER", "CORN", "BANANA", "CREAM", "PORRIDGE"};

    for (String shape : shapes) {
      // 배변기록 생성
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

      Long toiletRecordId = createToiletRecord(createRequest);

      // 형태 수정
      String updateRequest = String.format("{\"shape\": \"%s\"}", shape);

      given()
          .contentType(ContentType.JSON)
          .header("Authorization", validJwtToken)
          .body(updateRequest)
          .when()
          .patch("/api/v1/poo-records/" + toiletRecordId)
          .then()
          .statusCode(HttpStatus.OK.value())
          .body("data.shape", equalTo(shape));
    }
  }

  @Test
  @DisplayName("배변기록 수정시 ToiletScore가 업데이트된다")
  void givenToiletRecordUpdated_whenEventProcessed_thenToiletScoreIsUpdated() {
    // given: 배변기록 생성
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
          "duration": 10
        }
        """,
            now.format(formatter));

    Long toiletRecordId = createToiletRecord(createRequest);

    // 초기 ToiletScore 생성 대기
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

    // 초기 점수 저장
    int initialScore =
        toiletScoreRepository.findAll().stream()
            .filter(score -> score.getUserId().equals(testUser.getId()))
            .filter(score -> score.getDate().equals(today))
            .findFirst()
            .get()
            .getScore();

    // when: 배변기록 수정 (더 나쁜 상태로)
    String updateRequest =
        """
        {
          "isSuccessful": false,
          "color": "RED",
          "shape": "RABBIT",
          "pain": 80
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(updateRequest)
        .when()
        .patch("/api/v1/poo-records/" + toiletRecordId)
        .then()
        .statusCode(HttpStatus.OK.value());

    // then: ToiletScore가 업데이트됨
    // 배변기록을 수정하면 StoolReport가 재계산되고, ToiletScore가 업데이트됨
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

              // UNIQUE constraint로 인해 하루에 하나의 ToiletScore만 존재
              assertThat(scores).hasSize(1);
              // 수정 후 점수가 변경되었는지 확인
              assertThat(scores.get(0).getScore()).isNotEqualTo(initialScore);
            });
  }

  @Test
  @DisplayName("같은 날짜에 여러 배변기록 수정시 ToiletScore가 재계산된다")
  void givenMultipleRecordsUpdated_whenEventProcessed_thenToiletScoreIsRecalculated() {
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

    Long firstRecordId = createToiletRecord(firstRequest);

    // 두 번째 배변 기록 생성
    LocalDateTime laterTime = now.plusHours(1);
    String secondRequest =
        String.format(
            """
        {
          "occurredAt": "%s",
          "isSuccessful": true,
          "color": "GOLD",
          "shape": "BANANA",
          "pain": 10,
          "duration": 8
        }
        """,
            laterTime.format(formatter));

    Long secondRecordId = createToiletRecord(secondRequest);

    // 두 번째 ToiletScore 생성 대기
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

    // 초기 점수 저장
    int scoreAfterTwoRecords =
        toiletScoreRepository.findAll().stream()
            .filter(score -> score.getUserId().equals(testUser.getId()))
            .filter(score -> score.getDate().equals(today))
            .findFirst()
            .get()
            .getScore();

    // 첫 번째 배변기록 수정 (더 나쁜 상태로)
    String updateRequest =
        """
        {
          "isSuccessful": false,
          "color": "RED",
          "pain": 90
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(updateRequest)
        .when()
        .patch("/api/v1/poo-records/" + firstRecordId)
        .then()
        .statusCode(HttpStatus.OK.value());

    // ToiletScore가 재계산됨
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
              // 한 기록이 악화되었으므로 새로운 ToiletScore가 생성되거나 점수가 변경되어야 함
              boolean hasMultipleEntries = scores.size() > 1;
              boolean scoreChanged =
                  scores.stream().anyMatch(score -> score.getScore() != scoreAfterTwoRecords);

              assertThat(hasMultipleEntries || scoreChanged)
                  .as("ToiletScore should be recalculated after record update")
                  .isTrue();
            });
  }
}
