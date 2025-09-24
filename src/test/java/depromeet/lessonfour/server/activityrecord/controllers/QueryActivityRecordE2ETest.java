package depromeet.lessonfour.server.activityrecord.controllers;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

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

import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import depromeet.lessonfour.server.activityrecord.domain.vo.MealFood;
import depromeet.lessonfour.server.activityrecord.domain.vo.MealTime;
import depromeet.lessonfour.server.activityrecord.domain.vo.StressLevel;
import depromeet.lessonfour.server.activityrecord.infra.repository.JpaActivityRecordRepository;
import depromeet.lessonfour.server.auth.domain.vo.AccountContext;
import depromeet.lessonfour.server.auth.infra.security.jwt.JwtTokenGenerator;
import depromeet.lessonfour.server.food.domain.entity.Food;
import depromeet.lessonfour.server.food.infra.repository.FoodRepository;
import depromeet.lessonfour.server.user.domain.entity.User;
import depromeet.lessonfour.server.user.infra.repository.UserRepository;
import io.restassured.RestAssured;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql(
    scripts = "/sql/cleanup.sql",
    config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED),
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class QueryActivityRecordE2ETest {

  @LocalServerPort private int port;

  @Autowired private JwtTokenGenerator jwtTokenGenerator;
  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private FoodRepository foodRepository;
  @Autowired private JpaActivityRecordRepository activityRecordRepository;

  private String validJwtToken;
  private User testUser;
  private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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

  private ActivityRecord createTestActivityRecord(
      LocalDateTime activityAt,
      StressLevel stressLevel,
      int waterIntakeCups,
      List<MealFood> mealFoods) {
    return activityRecordRepository.save(
        ActivityRecord.createWithMeals(
            testUser, waterIntakeCups, stressLevel, activityAt, mealFoods));
  }

  @Test
  @DisplayName("특정 날짜의 생활 기록 조회 시 성공적으로 반환한다")
  void givenExistingActivityRecord_whenQueryActivityRecord_thenSuccess() {
    // Given
    LocalDate targetDate = LocalDate.of(2024, 1, 15);
    LocalDateTime activityAt = targetDate.atTime(14, 30, 0);

    List<Food> foods = foodRepository.findAll();
    List<MealFood> mealFoods =
        List.of(
            new MealFood(MealTime.BREAKFAST, foods.get(0)),
            new MealFood(MealTime.LUNCH, foods.get(1)));

    ActivityRecord savedRecord =
        createTestActivityRecord(activityAt, StressLevel.MEDIUM, 5, mealFoods);

    // When & Then
    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .queryParam("date", targetDate.format(dateFormatter))
        .when()
        .get("/api/v1/activity-records")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.id", equalTo(savedRecord.getId().intValue()))
        .body("data.waterIntakeCups", equalTo(5))
        .body("data.stressLevel", equalTo("MEDIUM"))
        .body("data.foods", hasSize(2))
        .body("data.foods[0].name", equalTo("사과"))
        .body("data.foods[1].name", equalTo("바나나"))
        .body("data.occurredAt", notNullValue());
  }

  @Test
  @DisplayName("존재하지 않는 날짜의 생활 기록 조회 시 404 에러를 반환한다")
  void givenNonExistingDate_whenQueryActivityRecord_thenNotFound() {
    // Given
    LocalDate nonExistingDate = LocalDate.of(2024, 12, 25);

    // When & Then
    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .queryParam("date", nonExistingDate.format(dateFormatter))
        .when()
        .get("/api/v1/activity-records")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.NOT_FOUND.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(404));
  }

  @Test
  @DisplayName("JWT 토큰 없이 조회 요청 시 401 에러를 반환한다")
  void givenNoAuthToken_whenQueryActivityRecord_thenUnauthorized() {
    // Given
    LocalDate targetDate = LocalDate.of(2024, 1, 15);

    // When & Then
    given()
        .log()
        .all()
        .queryParam("date", targetDate.format(dateFormatter))
        .when()
        .get("/api/v1/activity-records")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  @DisplayName("유효하지 않은 JWT 토큰으로 조회 요청 시 401 에러를 반환한다")
  void givenInvalidAuthToken_whenQueryActivityRecord_thenUnauthorized() {
    // Given
    LocalDate targetDate = LocalDate.of(2024, 1, 15);

    // When & Then
    given()
        .log()
        .all()
        .header("Authorization", "Bearer invalid-token")
        .queryParam("date", targetDate.format(dateFormatter))
        .when()
        .get("/api/v1/activity-records")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  @DisplayName("날짜 파라미터 없이 조회 요청 시 400 에러를 반환한다")
  void givenNoDateParameter_whenQueryActivityRecord_thenBadRequest() {
    // When & Then
    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .when()
        .get("/api/v1/activity-records")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  @DisplayName("유효하지 않은 날짜 형식으로 조회 요청 시 400 에러를 반환한다")
  void givenInvalidDateFormat_whenQueryActivityRecord_thenBadRequest() {
    // When & Then
    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .queryParam("date", "invalid-date-format")
        .when()
        .get("/api/v1/activity-records")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  @DisplayName("삭제된 생활 기록은 조회되지 않는다")
  void givenDeletedActivityRecord_whenQueryActivityRecord_thenNotFound() {
    // Given
    LocalDate targetDate = LocalDate.of(2024, 1, 15);
    LocalDateTime activityAt = targetDate.atTime(14, 30, 0);

    List<Food> foods = foodRepository.findAll();
    List<MealFood> mealFoods = List.of(new MealFood(MealTime.BREAKFAST, foods.get(0)));

    ActivityRecord savedRecord =
        createTestActivityRecord(activityAt, StressLevel.LOW, 3, mealFoods);
    savedRecord.delete(); // 소프트 삭제
    activityRecordRepository.save(savedRecord);

    // When & Then
    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .queryParam("date", targetDate.format(dateFormatter))
        .when()
        .get("/api/v1/activity-records")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.NOT_FOUND.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(404));
  }

  @Test
  @DisplayName("다른 사용자의 생활 기록은 조회되지 않는다")
  void givenOtherUserActivityRecord_whenQueryActivityRecord_thenNotFound() {
    // Given
    User otherUser = createTestUser("other@example.com", "password123", "otheruser");
    LocalDate targetDate = LocalDate.of(2024, 1, 15);
    LocalDateTime activityAt = targetDate.atTime(14, 30, 0);

    List<Food> foods = foodRepository.findAll();
    List<MealFood> mealFoods = List.of(new MealFood(MealTime.BREAKFAST, foods.get(0)));

    // 다른 사용자의 생활 기록 생성
    ActivityRecord otherUserRecord =
        activityRecordRepository.save(
            ActivityRecord.createWithMeals(otherUser, 3, StressLevel.LOW, activityAt, mealFoods));

    // When & Then - 현재 사용자로 조회 시 찾을 수 없음
    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .queryParam("date", targetDate.format(dateFormatter))
        .when()
        .get("/api/v1/activity-records")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.NOT_FOUND.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(404));
  }

  @Test
  @DisplayName("음식 목록이 비어있는 생활 기록도 정상 조회된다")
  void givenActivityRecordWithNoFoods_whenQueryActivityRecord_thenSuccess() {
    // Given
    LocalDate targetDate = LocalDate.of(2024, 1, 15);
    LocalDateTime activityAt = targetDate.atTime(14, 30, 0);

    ActivityRecord savedRecord =
        createTestActivityRecord(activityAt, StressLevel.HIGH, 8, List.of());

    // When & Then
    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .queryParam("date", targetDate.format(dateFormatter))
        .when()
        .get("/api/v1/activity-records")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.id", equalTo(savedRecord.getId().intValue()))
        .body("data.waterIntakeCups", equalTo(8))
        .body("data.stressLevel", equalTo("HIGH"))
        .body("data.foods", hasSize(0))
        .body("data.occurredAt", notNullValue());
  }

  @Test
  @DisplayName("모든 스트레스 레벨의 생활 기록이 정상 조회된다")
  void givenAllStressLevels_whenQueryActivityRecord_thenSuccess() {
    // Given & When & Then for each stress level
    StressLevel[] stressLevels = StressLevel.values();

    for (int i = 0; i < stressLevels.length; i++) {
      LocalDate targetDate = LocalDate.of(2024, 1, 15 + i);
      LocalDateTime activityAt = targetDate.atTime(14, 30, 0);
      StressLevel stressLevel = stressLevels[i];

      ActivityRecord savedRecord = createTestActivityRecord(activityAt, stressLevel, 3, List.of());

      given()
          .log()
          .all()
          .header("Authorization", validJwtToken)
          .queryParam("date", targetDate.format(dateFormatter))
          .when()
          .get("/api/v1/activity-records")
          .then()
          .log()
          .all()
          .statusCode(HttpStatus.OK.value())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .body("status", equalTo(200))
          .body("data.id", equalTo(savedRecord.getId().intValue()))
          .body("data.stressLevel", equalTo(stressLevel.name()));
    }
  }

  @Test
  @DisplayName("물 섭취량이 0인 생활 기록도 정상 조회된다")
  void givenZeroWaterIntake_whenQueryActivityRecord_thenSuccess() {
    // Given
    LocalDate targetDate = LocalDate.of(2024, 1, 15);
    LocalDateTime activityAt = targetDate.atTime(14, 30, 0);

    ActivityRecord savedRecord =
        createTestActivityRecord(activityAt, StressLevel.MEDIUM, 0, List.of());

    // When & Then
    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .queryParam("date", targetDate.format(dateFormatter))
        .when()
        .get("/api/v1/activity-records")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.id", equalTo(savedRecord.getId().intValue()))
        .body("data.waterIntakeCups", equalTo(0))
        .body("data.stressLevel", equalTo("MEDIUM"));
  }

  @Test
  @DisplayName("최대 물 섭취량인 생활 기록도 정상 조회된다")
  void givenMaxWaterIntake_whenQueryActivityRecord_thenSuccess() {
    // Given
    LocalDate targetDate = LocalDate.of(2024, 1, 15);
    LocalDateTime activityAt = targetDate.atTime(14, 30, 0);

    ActivityRecord savedRecord =
        createTestActivityRecord(activityAt, StressLevel.MEDIUM, 10, List.of());

    // When & Then
    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .queryParam("date", targetDate.format(dateFormatter))
        .when()
        .get("/api/v1/activity-records")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.id", equalTo(savedRecord.getId().intValue()))
        .body("data.waterIntakeCups", equalTo(10))
        .body("data.stressLevel", equalTo("MEDIUM"));
  }

  @Test
  @DisplayName("모든 식사 시간 타입의 음식이 포함된 생활 기록이 정상 조회된다")
  void givenAllMealTimes_whenQueryActivityRecord_thenSuccess() {
    // Given
    LocalDate targetDate = LocalDate.of(2024, 1, 15);
    LocalDateTime activityAt = targetDate.atTime(14, 30, 0);

    List<Food> foods = foodRepository.findAll();
    List<MealFood> mealFoods =
        List.of(
            new MealFood(MealTime.BREAKFAST, foods.get(0)),
            new MealFood(MealTime.LUNCH, foods.get(1)),
            new MealFood(MealTime.DINNER, foods.get(2)),
            new MealFood(MealTime.SNACK, foods.get(3)));

    ActivityRecord savedRecord =
        createTestActivityRecord(activityAt, StressLevel.MEDIUM, 5, mealFoods);

    // When & Then
    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .queryParam("date", targetDate.format(dateFormatter))
        .when()
        .get("/api/v1/activity-records")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.id", equalTo(savedRecord.getId().intValue()))
        .body("data.foods", hasSize(4))
        .body("data.foods[0].name", equalTo("사과"))
        .body("data.foods[1].name", equalTo("바나나"))
        .body("data.foods[2].name", equalTo("샐러드"))
        .body("data.foods[3].name", equalTo("햄버거"));
  }
}
