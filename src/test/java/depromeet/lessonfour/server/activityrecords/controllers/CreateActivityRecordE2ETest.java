package depromeet.lessonfour.server.activityrecords.controllers;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

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

import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import depromeet.lessonfour.server.activityrecord.infra.repository.ActivityRecordRepository;
import depromeet.lessonfour.server.auth.domain.vo.AccountContext;
import depromeet.lessonfour.server.auth.infra.security.jwt.JwtTokenGenerator;
import depromeet.lessonfour.server.food.domain.entity.Food;
import depromeet.lessonfour.server.food.infra.repository.FoodRepository;
import depromeet.lessonfour.server.user.domain.entity.User;
import depromeet.lessonfour.server.user.infra.repository.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql(
    scripts = "/sql/cleanup.sql",
    config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED),
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class CreateActivityRecordE2ETest {

  @LocalServerPort private int port;

  @Autowired private JwtTokenGenerator jwtTokenGenerator;
  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private FoodRepository foodRepository;
  @Autowired private ActivityRecordRepository activityRecordRepository;

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

    // 테스트용 Food 데이터 생성
    List<Food> foods =
        List.of(
            Food.createForTest("사과", 4.5),
            Food.createForTest("바나나", 4.0),
            Food.createForTest("샐러드", 5.0),
            Food.createForTest("햄버거", 2.0));
    foodRepository.saveAll(foods);
  }

  private User createTestUser(String email, String password, String nickname) {
    User user = User.register(email, nickname, passwordEncoder.encode(password));
    return userRepository.save(user);
  }

  @Test
  @DisplayName("유효한 생활 기록 생성 요청시 성공적으로 생성된다")
  void givenValidActivityRecordRequest_whenCreateActivityRecord_thenSuccess() {
    LocalDateTime now = LocalDateTime.now();
    String createRequest =
        String.format(
            """
        {
          "foods": [
            {
              "id": 1,
              "mealTime": "BREAKFAST"
            },
            {
              "id": 2,
              "mealTime": "LUNCH"
            }
          ],
          "water": 5,
          "stress": "MEDIUM",
          "occurredAt": "%s"
        }
        """,
            now.format(formatter));

    given()
        .log()
        .all() // 요청 전체 로그 출력
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(createRequest)
        .when()
        .post("/api/v1/activity-records")
        .then()
        .log()
        .all() // 응답 전체 로그 출력
        .statusCode(HttpStatus.CREATED.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(201));
  }

  @Test
  @DisplayName("JWT 토큰 없이 요청시 401 에러를 반환한다")
  void givenNoAuthToken_whenCreateActivityRecord_thenUnauthorized() {
    LocalDateTime now = LocalDateTime.now();
    String createRequest =
        String.format(
            """
        {
          "foods": [
            {
              "id": 1,
              "mealTime": "BREAKFAST"
            }
          ],
          "water": 3,
          "stress": "LOW",
          "occurredAt": "%s"
        }
        """,
            now.format(formatter));

    given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .when()
        .post("/api/v1/activity-records")
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  @DisplayName("유효하지 않은 JWT 토큰으로 요청시 401 에러를 반환한다")
  void givenInvalidAuthToken_whenCreateActivityRecord_thenUnauthorized() {
    LocalDateTime now = LocalDateTime.now();
    String createRequest =
        String.format(
            """
        {
          "foods": [
            {
              "id": 1,
              "mealTime": "BREAKFAST"
            }
          ],
          "water": 3,
          "stress": "LOW",
          "occurredAt": "%s"
        }
        """,
            now.format(formatter));

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer invalid-token")
        .body(createRequest)
        .when()
        .post("/api/v1/activity-records")
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  @DisplayName("음식 목록이 null인 경우 400 에러를 반환한다")
  void givenNullSelectedFoods_whenCreateActivityRecord_thenBadRequest() {
    LocalDateTime now = LocalDateTime.now();
    String createRequest =
        String.format(
            """
        {
          "foods": null,
          "water": 3,
          "stress": "LOW",
          "occurredAt": "%s"
        }
        """,
            now.format(formatter));

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(createRequest)
        .when()
        .post("/api/v1/activity-records")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("message", containsString("음식 목록은 필수입니다"));
  }

  @Test
  @DisplayName("물 섭취량이 음수인 경우 400 에러를 반환한다")
  void givenNegativeWaterAmount_whenCreateActivityRecord_thenBadRequest() {
    LocalDateTime now = LocalDateTime.now();
    String createRequest =
        String.format(
            """
        {
          "foods": [
            {
              "id": 1,
              "mealTime": "BREAKFAST"
            }
          ],
          "water": -1,
          "stress": "LOW",
          "occurredAt": "%s"
        }
        """,
            now.format(formatter));

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(createRequest)
        .when()
        .post("/api/v1/activity-records")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("message", containsString("마신 물의 잔 수는 양수여야 합니다"));
  }

  @Test
  @DisplayName("스트레스 지수가 null인 경우 400 에러를 반환한다")
  void givenNullStressLevel_whenCreateActivityRecord_thenBadRequest() {
    LocalDateTime now = LocalDateTime.now();
    String createRequest =
        String.format(
            """
        {
          "foods": [
            {
              "id": 1,
              "mealTime": "BREAKFAST"
            }
          ],
          "water": 3,
          "stress": null,
          "occurredAt": "%s"
        }
        """,
            now.format(formatter));

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(createRequest)
        .when()
        .post("/api/v1/activity-records")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("message", containsString("스트레스 지수는 필수입니다"));
  }

  @Test
  @DisplayName("유효하지 않은 스트레스 지수인 경우 400 에러를 반환한다")
  void givenInvalidStressLevel_whenCreateActivityRecord_thenBadRequest() {
    LocalDateTime now = LocalDateTime.now();
    String createRequest =
        String.format(
            """
        {
          "foods": [
            {
              "id": 1,
              "mealTime": "BREAKFAST"
            }
          ],
          "water": 3,
          "stress": "INVALID_STRESS",
          "occurredAt": "%s"
        }
        """,
            now.format(formatter));

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(createRequest)
        .when()
        .post("/api/v1/activity-records")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("message", containsString("요청 본문이 올바르지 않습니다"));
  }

  @Test
  @DisplayName("선택한 날짜와 시간이 null인 경우 400 에러를 반환한다")
  void givenNullSelectedWhen_whenCreateActivityRecord_thenBadRequest() {
    String createRequest =
        """
        {
          "foods": [
            {
              "id": 1,
              "mealTime": "BREAKFAST"
            }
          ],
          "water": 3,
          "stress": "LOW",
          "occurredAt": null
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(createRequest)
        .when()
        .post("/api/v1/activity-records")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("message", containsString("선택한 날짜와 시간은 필수입니다"));
  }

  @Test
  @DisplayName("유효하지 않은 날짜 형식인 경우 400 에러를 반환한다")
  void givenInvalidDateFormat_whenCreateActivityRecord_thenBadRequest() {
    String createRequest =
        """
        {
          "foods": [
            {
              "id": 1,
              "mealTime": "BREAKFAST"
            }
          ],
          "water": 3,
          "stress": "LOW",
          "occurredAt": "invalid-date-format"
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(createRequest)
        .when()
        .post("/api/v1/activity-records")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  @DisplayName("음식 ID가 null인 경우 400 에러를 반환한다")
  void givenNullFoodId_whenCreateActivityRecord_thenBadRequest() {
    LocalDateTime now = LocalDateTime.now();
    String createRequest =
        String.format(
            """
        {
          "foods": [
            {
              "id": null,
              "mealTime": "BREAKFAST"
            }
          ],
          "water": 3,
          "stress": "LOW",
          "occurredAt": "%s"
        }
        """,
            now.format(formatter));

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(createRequest)
        .when()
        .post("/api/v1/activity-records")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("message", containsString("음식 id는 필수입니다"));
  }

  @Test
  @DisplayName("식사 시간이 null인 경우 400 에러를 반환한다")
  void givenNullMealTime_whenCreateActivityRecord_thenBadRequest() {
    LocalDateTime now = LocalDateTime.now();
    String createRequest =
        String.format(
            """
        {
          "foods": [
            {
              "id": 1,
              "mealTime": null
            }
          ],
          "water": 3,
          "stress": "LOW",
          "occurredAt": "%s"
        }
        """,
            now.format(formatter));

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(createRequest)
        .when()
        .post("/api/v1/activity-records")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("message", containsString("식사 시간은 필수입니다"));
  }

  @Test
  @DisplayName("유효하지 않은 식사 시간인 경우 400 에러를 반환한다")
  void givenInvalidMealTime_whenCreateActivityRecord_thenBadRequest() {
    LocalDateTime now = LocalDateTime.now();
    String createRequest =
        String.format(
            """
        {
          "foods": [
            {
              "foodId": 1,
              "mealTime": "INVALID_MEAL_TIME"
            }
          ],
          "water": 3,
          "stress": "LOW",
          "occurredAt": "%s"
        }
        """,
            now.format(formatter));

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(createRequest)
        .when()
        .post("/api/v1/activity-records")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("message", containsString("요청 본문이 올바르지 않습니다"));
  }

  @Test
  @DisplayName("빈 음식 목록으로 요청시 성공한다")
  void givenEmptyFoodList_whenCreateActivityRecord_thenSuccess() {
    LocalDateTime now = LocalDateTime.now();
    String createRequest =
        String.format(
            """
        {
          "foods": [],
          "water": 8,
          "stress": "HIGH",
          "occurredAt": "%s"
        }
        """,
            now.format(formatter));

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(createRequest)
        .when()
        .post("/api/v1/activity-records")
        .then()
        .statusCode(HttpStatus.CREATED.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(201));
  }

  @Test
  @DisplayName("물 섭취량이 0인 경우 성공한다")
  void givenZeroWaterAmount_whenCreateActivityRecord_thenSuccess() {
    LocalDateTime now = LocalDateTime.now();
    String createRequest =
        String.format(
            """
        {
          "foods": [
            {
              "id": 1,
              "mealTime": "SNACK"
            }
          ],
          "water": 0,
          "stress": "VERY_LOW",
          "occurredAt": "%s"
        }
        """,
            now.format(formatter));

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(createRequest)
        .when()
        .post("/api/v1/activity-records")
        .then()
        .statusCode(HttpStatus.CREATED.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(201));
  }

  @Test
  @DisplayName("모든 식사 시간과 스트레스 레벨로 요청시 성공한다")
  void givenAllMealTimesAndStressLevels_whenCreateActivityRecord_thenSuccess() {
    LocalDateTime now = LocalDateTime.now();
    String createRequest =
        String.format(
            """
        {
          "foods": [
            {
              "id": 1,
              "mealTime": "BREAKFAST"
            },
            {
              "id": 2,
              "mealTime": "LUNCH"
            },
            {
              "id": 3,
              "mealTime": "DINNER"
            },
            {
              "id": 4,
              "mealTime": "SNACK"
            }
          ],
          "water": 10,
          "stress": "VERY_HIGH",
          "occurredAt": "%s"
        }
        """,
            now.format(formatter));

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(createRequest)
        .when()
        .post("/api/v1/activity-records")
        .then()
        .statusCode(HttpStatus.CREATED.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(201));
  }

  @Test
  @DisplayName("JSON 형식이 아닌 요청시 400 에러를 반환한다")
  void givenNonJsonRequest_whenCreateActivityRecord_thenBadRequest() {
    String invalidRequest = "not-json-format";

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(invalidRequest)
        .when()
        .post("/api/v1/activity-records")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  @DisplayName("Content-Type이 application/json이 아닌 경우 415 에러를 반환한다")
  void givenNonJsonContentType_whenCreateActivityRecord_thenUnsupportedMediaType() {
    String requestBody = "water=3&stress=LOW";

    given()
        .contentType(ContentType.URLENC)
        .header("Authorization", validJwtToken)
        .body(requestBody)
        .when()
        .post("/api/v1/activity-records")
        .then()
        .statusCode(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());
  }

  @Test
  @DisplayName("특정 날짜의 생활 기록을 생성했다가 삭제한 후 다시 생성하는 경우 성공한다")
  void givenCreateDeleteRecreate_whenCreateActivityRecord_thenSuccess() {
    LocalDateTime specificDate = LocalDateTime.of(2024, 1, 15, 14, 30, 0);
    String createRequest =
        String.format(
            """
        {
          "foods": [
            {
              "id": 1,
              "mealTime": "LUNCH"
            }
          ],
          "water": 3,
          "stress": "MEDIUM",
          "occurredAt": "%s"
        }
        """,
            specificDate.format(formatter));

    // 1. 첫 번째 생활 기록 생성
    given()
        .log()
        .all()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(createRequest)
        .when()
        .post("/api/v1/activity-records")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.CREATED.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(201));

    // 생성된 생활 기록 ID 추출 - activityAt으로 조회
    LocalDateTime startOfDay = specificDate.toLocalDate().atStartOfDay();
    LocalDateTime endOfDay = specificDate.toLocalDate().atTime(23, 59, 59);

    List<ActivityRecord> activityRecords =
        activityRecordRepository.findAll().stream()
            .filter(record -> record.getUser().getId().equals(testUser.getId()))
            .filter(record -> !record.isDeleted())
            .filter(
                record ->
                    record.getActivityAt().isAfter(startOfDay)
                        && record.getActivityAt().isBefore(endOfDay))
            .toList();

    Long activityRecordId =
        activityRecords.stream()
            .findFirst()
            .map(ActivityRecord::getId)
            .orElseThrow(() -> new RuntimeException("생성된 ActivityRecord를 찾을 수 없습니다"));

    // 2. 생성된 생활 기록 삭제
    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .when()
        .delete("/api/v1/activity-records/{activityRecordId}", activityRecordId)
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE);

    // 3. 같은 날짜로 다시 생활 기록 생성 (성공해야 함)
    given()
        .log()
        .all()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(createRequest)
        .when()
        .post("/api/v1/activity-records")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.CREATED.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(201));

    // 4. 재생성된 기록이 실제로 존재하는지 확인
    List<ActivityRecord> recreatedRecords =
        activityRecordRepository.findAll().stream()
            .filter(record -> record.getUser().getId().equals(testUser.getId()))
            .filter(record -> !record.isDeleted())
            .filter(
                record ->
                    record.getActivityAt().isAfter(startOfDay)
                        && record.getActivityAt().isBefore(endOfDay))
            .toList();

    assert !recreatedRecords.isEmpty() : "재생성된 ActivityRecord가 존재하지 않습니다";
  }
}
