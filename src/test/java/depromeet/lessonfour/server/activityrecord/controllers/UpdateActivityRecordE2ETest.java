package depromeet.lessonfour.server.activityrecord.controllers;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
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
import depromeet.lessonfour.server.activityrecord.domain.vo.MealFood;
import depromeet.lessonfour.server.activityrecord.domain.vo.MealTime;
import depromeet.lessonfour.server.activityrecord.domain.vo.StressLevel;
import depromeet.lessonfour.server.activityrecord.infra.repository.JpaActivityRecordRepository;
import depromeet.lessonfour.server.auth.domain.vo.AccountContext;
import depromeet.lessonfour.server.auth.infra.security.jwt.JwtTokenGenerator;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.food.domain.entity.Food;
import depromeet.lessonfour.server.food.infra.repository.FoodRepository;
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
class UpdateActivityRecordE2ETest {

  @LocalServerPort private int port;

  @Autowired private JwtTokenGenerator jwtTokenGenerator;
  @Autowired private UserRepository jpaUserRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private FoodRepository foodRepository;
  @Autowired private JpaActivityRecordRepository activityRecordRepository;

  private String validJwtToken;
  private User testUser;
  private User otherUser;
  private Long appleId;
  private Long bananaId;
  private Long saladId;
  private Long burgerId;
  private final DateTimeFormatter formatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

    // 실제 사용자 생성 및 JWT 토큰 생성
    testUser = createTestUser("test@example.com", "password123", "testuser");
    otherUser = createTestUser("other@example.com", "password456", "otheruser");
    validJwtToken = "Bearer " + jwtTokenGenerator.generateAccessToken(AccountContext.of(testUser));

    // Food 생성
    List<Food> foods =
        List.of(
            Food.builder().name("사과").score(4.5).build(),
            Food.builder().name("바나나").score(4.0).build(),
            Food.builder().name("샐러드").score(5.0).build(),
            Food.builder().name("햄버거").score(2.0).build());
    List<Food> savedFoods = foodRepository.saveAll(foods);
    appleId = savedFoods.get(0).getId();
    bananaId = savedFoods.get(1).getId();
    saladId = savedFoods.get(2).getId();
    burgerId = savedFoods.get(3).getId();
  }

  private User createTestUser(String email, String password, String nickname) {
    User user = User.register(email, nickname, passwordEncoder.encode(password));
    return jpaUserRepository.save(user);
  }

  private ActivityRecord createTestActivityRecord(
      User user, LocalDateTime occurredAt, int water, StressLevel stress) {
    ActivityRecord record =
        ActivityRecord.createWithMeals(
            user.getId(),
            water,
            stress,
            ActivityAt.from(occurredAt),
            List.of(
                new MealFood(MealTime.BREAKFAST, foodRepository.findById(appleId).orElseThrow())));
    return activityRecordRepository.save(record);
  }

  @Test
  @DisplayName("유효한 생활 기록 수정 요청시 성공적으로 수정된다")
  void givenValidUpdateRequest_whenUpdateActivityRecord_thenSuccess() {
    // given: 기존 생활 기록 생성
    LocalDateTime now = LocalDateTime.now();
    ActivityRecord record = createTestActivityRecord(testUser, now, 3, StressLevel.LOW);

    String updateRequest =
        String.format(
            """
        {
          "foods": [
            {
              "id": %d,
              "mealTime": "LUNCH"
            },
            {
              "id": %d,
              "mealTime": "DINNER"
            }
          ],
          "water": 7,
          "stress": "HIGH"
        }
        """,
            bananaId, saladId);

    // when & then
    given()
        .log()
        .all()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(updateRequest)
        .when()
        .patch("/api/v1/activity-records/{activityRecordId}", record.getId())
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200));

    ActivityRecord updatedRecord =
        activityRecordRepository.findByIdWithFoodRecords(record.getId()).orElseThrow();
    assertThat(updatedRecord.getWaterIntakeCups()).isEqualTo(7);
    assertThat(updatedRecord.getStressLevel()).isEqualTo(StressLevel.HIGH);
    assertThat(updatedRecord.getFoodRecords()).hasSize(2);
  }

  @Test
  @DisplayName("음식 목록만 수정 요청시 성공적으로 수정된다")
  void givenOnlyFoodsUpdate_whenUpdateActivityRecord_thenSuccess() {
    // given
    LocalDateTime now = LocalDateTime.now();
    ActivityRecord record = createTestActivityRecord(testUser, now, 5, StressLevel.MEDIUM);

    String updateRequest =
        String.format(
            """
        {
          "foods": [
            {
              "id": %d,
              "mealTime": "SNACK"
            }
          ]
        }
        """,
            burgerId);

    // when & then
    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(updateRequest)
        .when()
        .patch("/api/v1/activity-records/{activityRecordId}", record.getId())
        .then()
        .statusCode(HttpStatus.OK.value());

    // 음식은 변경, 물과 스트레스는 유지
    ActivityRecord updatedRecord =
        activityRecordRepository.findByIdWithFoodRecords(record.getId()).orElseThrow();
    assertThat(updatedRecord.getWaterIntakeCups()).isEqualTo(5);
    assertThat(updatedRecord.getStressLevel()).isEqualTo(StressLevel.MEDIUM);
    assertThat(updatedRecord.getFoodRecords()).hasSize(1);
  }

  @Test
  @DisplayName("물 섭취량만 수정 요청시 성공적으로 수정된다")
  void givenOnlyWaterUpdate_whenUpdateActivityRecord_thenSuccess() {
    // given
    LocalDateTime now = LocalDateTime.now();
    ActivityRecord record = createTestActivityRecord(testUser, now, 3, StressLevel.LOW);

    String updateRequest = """
        {
          "water": 9
        }
        """;

    // when & then
    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(updateRequest)
        .when()
        .patch("/api/v1/activity-records/{activityRecordId}", record.getId())
        .then()
        .statusCode(HttpStatus.OK.value());

    // 물만 변경, 스트레스와 음식은 유지
    ActivityRecord updatedRecord =
        activityRecordRepository.findByIdWithFoodRecords(record.getId()).orElseThrow();
    assertThat(updatedRecord.getWaterIntakeCups()).isEqualTo(9);
    assertThat(updatedRecord.getStressLevel()).isEqualTo(StressLevel.LOW);
    assertThat(updatedRecord.getFoodRecords()).hasSize(1);
  }

  @Test
  @DisplayName("스트레스만 수정 요청시 성공적으로 수정된다")
  void givenOnlyStressUpdate_whenUpdateActivityRecord_thenSuccess() {
    // given
    LocalDateTime now = LocalDateTime.now();
    ActivityRecord record = createTestActivityRecord(testUser, now, 5, StressLevel.MEDIUM);

    String updateRequest = """
        {
          "stress": "VERY_HIGH"
        }
        """;

    // when & then
    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(updateRequest)
        .when()
        .patch("/api/v1/activity-records/{activityRecordId}", record.getId())
        .then()
        .statusCode(HttpStatus.OK.value());

    // 스트레스만 변경, 물과 음식은 유지
    ActivityRecord updatedRecord =
        activityRecordRepository.findByIdWithFoodRecords(record.getId()).orElseThrow();
    assertThat(updatedRecord.getWaterIntakeCups()).isEqualTo(5);
    assertThat(updatedRecord.getStressLevel()).isEqualTo(StressLevel.VERY_HIGH);
    assertThat(updatedRecord.getFoodRecords()).hasSize(1);
  }

  @Test
  @DisplayName("음식 목록을 빈 배열로 수정하면 기존 음식이 모두 제거된다")
  void givenEmptyFoodList_whenUpdateActivityRecord_thenAllFoodsRemoved() {
    // given
    LocalDateTime now = LocalDateTime.now();
    ActivityRecord record = createTestActivityRecord(testUser, now, 5, StressLevel.MEDIUM);

    String updateRequest = """
        {
          "foods": []
        }
        """;

    // when & then
    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(updateRequest)
        .when()
        .patch("/api/v1/activity-records/{activityRecordId}", record.getId())
        .then()
        .statusCode(HttpStatus.OK.value());

    // 음식 목록이 비어있어야 함
    ActivityRecord updatedRecord =
        activityRecordRepository.findByIdWithFoodRecords(record.getId()).orElseThrow();
    assertThat(updatedRecord.getFoodRecords()).isEmpty();
  }

  @Test
  @DisplayName("JWT 토큰 없이 수정 요청시 401 에러를 반환한다")
  void givenNoAuthToken_whenUpdateActivityRecord_thenUnauthorized() {
    // given
    LocalDateTime now = LocalDateTime.now();
    ActivityRecord record = createTestActivityRecord(testUser, now, 5, StressLevel.MEDIUM);

    String updateRequest = """
        {
          "water": 7
        }
        """;

    // when & then
    given()
        .contentType(ContentType.JSON)
        .body(updateRequest)
        .when()
        .patch("/api/v1/activity-records/{activityRecordId}", record.getId())
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  @DisplayName("유효하지 않은 JWT 토큰으로 수정 요청시 401 에러를 반환한다")
  void givenInvalidAuthToken_whenUpdateActivityRecord_thenUnauthorized() {
    // given
    LocalDateTime now = LocalDateTime.now();
    ActivityRecord record = createTestActivityRecord(testUser, now, 5, StressLevel.MEDIUM);

    String updateRequest = """
        {
          "water": 7
        }
        """;

    // when & then
    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer invalid-token")
        .body(updateRequest)
        .when()
        .patch("/api/v1/activity-records/{activityRecordId}", record.getId())
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  @DisplayName("다른 사용자의 생활 기록 수정 시도시 실패한다")
  void givenOtherUserRecord_whenUpdateActivityRecord_thenForbidden() {
    // given: 다른 사용자의 레코드 생성
    LocalDateTime now = LocalDateTime.now();
    ActivityRecord otherUserRecord =
        createTestActivityRecord(otherUser, now, 5, StressLevel.MEDIUM);

    String updateRequest = """
        {
          "water": 7
        }
        """;

    // when & then: testUser의 토큰으로 otherUser의 레코드 수정 시도
    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(updateRequest)
        .when()
        .patch("/api/v1/activity-records/{activityRecordId}", otherUserRecord.getId())
        .then()
        .statusCode(HttpStatus.FORBIDDEN.value());
  }

  @Test
  @DisplayName("물 섭취량이 음수인 경우 400 에러를 반환한다")
  void givenNegativeWaterAmount_whenUpdateActivityRecord_thenBadRequest() {
    // given
    LocalDateTime now = LocalDateTime.now();
    ActivityRecord record = createTestActivityRecord(testUser, now, 5, StressLevel.MEDIUM);

    String updateRequest = """
        {
          "water": -1
        }
        """;

    // when & then
    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(updateRequest)
        .when()
        .patch("/api/v1/activity-records/{activityRecordId}", record.getId())
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("message", containsString("마신 물의 잔 수는 0 이상이어야 합니다"));
  }

  @Test
  @DisplayName("유효하지 않은 스트레스 지수인 경우 400 에러를 반환한다")
  void givenInvalidStressLevel_whenUpdateActivityRecord_thenBadRequest() {
    // given
    LocalDateTime now = LocalDateTime.now();
    ActivityRecord record = createTestActivityRecord(testUser, now, 5, StressLevel.MEDIUM);

    String updateRequest = """
        {
          "stress": "INVALID_STRESS"
        }
        """;

    // when & then
    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(updateRequest)
        .when()
        .patch("/api/v1/activity-records/{activityRecordId}", record.getId())
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("message", containsString("요청 본문이 올바르지 않습니다"));
  }

  @Test
  @DisplayName("음식 ID가 null인 경우 400 에러를 반환한다")
  void givenNullFoodId_whenUpdateActivityRecord_thenBadRequest() {
    // given
    LocalDateTime now = LocalDateTime.now();
    ActivityRecord record = createTestActivityRecord(testUser, now, 5, StressLevel.MEDIUM);

    String updateRequest =
        """
        {
          "foods": [
            {
              "id": null,
              "mealTime": "BREAKFAST"
            }
          ]
        }
        """;

    // when & then
    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(updateRequest)
        .when()
        .patch("/api/v1/activity-records/{activityRecordId}", record.getId())
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("message", containsString("음식 id는 필수입니다"));
  }

  @Test
  @DisplayName("식사 시간이 null인 경우 400 에러를 반환한다")
  void givenNullMealTime_whenUpdateActivityRecord_thenBadRequest() {
    // given
    LocalDateTime now = LocalDateTime.now();
    ActivityRecord record = createTestActivityRecord(testUser, now, 5, StressLevel.MEDIUM);

    String updateRequest =
        String.format(
            """
        {
          "foods": [
            {
              "id": %d,
              "mealTime": null
            }
          ]
        }
        """,
            appleId);

    // when & then
    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(updateRequest)
        .when()
        .patch("/api/v1/activity-records/{activityRecordId}", record.getId())
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("message", containsString("식사 시간은 필수입니다"));
  }

  @Test
  @DisplayName("유효하지 않은 식사 시간인 경우 400 에러를 반환한다")
  void givenInvalidMealTime_whenUpdateActivityRecord_thenBadRequest() {
    // given
    LocalDateTime now = LocalDateTime.now();
    ActivityRecord record = createTestActivityRecord(testUser, now, 5, StressLevel.MEDIUM);

    String updateRequest =
        String.format(
            """
        {
          "foods": [
            {
              "id": %d,
              "mealTime": "INVALID_MEAL_TIME"
            }
          ]
        }
        """,
            appleId);

    // when & then
    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(updateRequest)
        .when()
        .patch("/api/v1/activity-records/{activityRecordId}", record.getId())
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("message", containsString("요청 본문이 올바르지 않습니다"));
  }

  @Test
  @DisplayName("존재하지 않는 생활 기록 ID로 수정 요청시 404 에러를 반환한다")
  void givenNonExistentRecordId_whenUpdateActivityRecord_thenNotFound() {
    // given
    Long nonExistentId = 99999L;

    String updateRequest = """
        {
          "water": 7
        }
        """;

    // when & then
    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(updateRequest)
        .when()
        .patch("/api/v1/activity-records/{activityRecordId}", nonExistentId)
        .then()
        .statusCode(HttpStatus.NOT_FOUND.value());
  }

  @Test
  @DisplayName("JSON 형식이 아닌 요청시 400 에러를 반환한다")
  void givenNonJsonRequest_whenUpdateActivityRecord_thenBadRequest() {
    // given
    LocalDateTime now = LocalDateTime.now();
    ActivityRecord record = createTestActivityRecord(testUser, now, 5, StressLevel.MEDIUM);

    String invalidRequest = "not-json-format";

    // when & then
    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(invalidRequest)
        .when()
        .patch("/api/v1/activity-records/{activityRecordId}", record.getId())
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  @DisplayName("Content-Type이 application/json이 아닌 경우 415 에러를 반환한다")
  void givenNonJsonContentType_whenUpdateActivityRecord_thenUnsupportedMediaType() {
    // given
    LocalDateTime now = LocalDateTime.now();
    ActivityRecord record = createTestActivityRecord(testUser, now, 5, StressLevel.MEDIUM);

    String requestBody = "water=7";

    // when & then
    given()
        .contentType(ContentType.URLENC)
        .header("Authorization", validJwtToken)
        .body(requestBody)
        .when()
        .patch("/api/v1/activity-records/{activityRecordId}", record.getId())
        .then()
        .statusCode(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());
  }

  @Test
  @DisplayName("모든 필드를 null로 수정 요청시 기존 값이 유지된다")
  void givenAllNullFields_whenUpdateActivityRecord_thenKeepOriginalValues() {
    // given
    LocalDateTime now = LocalDateTime.now();
    ActivityRecord record = createTestActivityRecord(testUser, now, 5, StressLevel.MEDIUM);

    String updateRequest =
        """
        {
          "foods": null,
          "water": null,
          "stress": null
        }
        """;

    // when & then
    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(updateRequest)
        .when()
        .patch("/api/v1/activity-records/{activityRecordId}", record.getId())
        .then()
        .statusCode(HttpStatus.OK.value());

    // 모든 값이 유지되어야 함
    ActivityRecord updatedRecord =
        activityRecordRepository.findByIdWithFoodRecords(record.getId()).orElseThrow();
    assertThat(updatedRecord.getWaterIntakeCups()).isEqualTo(5);
    assertThat(updatedRecord.getStressLevel()).isEqualTo(StressLevel.MEDIUM);
    assertThat(updatedRecord.getFoodRecords()).hasSize(1);
  }

  @Test
  @DisplayName("모든 식사 시간으로 음식 수정 요청시 성공한다")
  void givenAllMealTimes_whenUpdateActivityRecord_thenSuccess() {
    // given
    LocalDateTime now = LocalDateTime.now();
    ActivityRecord record = createTestActivityRecord(testUser, now, 5, StressLevel.MEDIUM);

    String updateRequest =
        String.format(
            """
        {
          "foods": [
            {
              "id": %d,
              "mealTime": "BREAKFAST"
            },
            {
              "id": %d,
              "mealTime": "LUNCH"
            },
            {
              "id": %d,
              "mealTime": "DINNER"
            },
            {
              "id": %d,
              "mealTime": "SNACK"
            }
          ]
        }
        """,
            appleId, bananaId, saladId, burgerId);

    // when & then
    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(updateRequest)
        .when()
        .patch("/api/v1/activity-records/{activityRecordId}", record.getId())
        .then()
        .statusCode(HttpStatus.OK.value());

    // 4개의 음식이 모두 추가되었는지 확인
    ActivityRecord updatedRecord =
        activityRecordRepository.findByIdWithFoodRecords(record.getId()).orElseThrow();
    assertThat(updatedRecord.getFoodRecords()).hasSize(4);
  }
}
