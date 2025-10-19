package depromeet.lessonfour.server.recordquery.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

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
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.food.domain.entity.Food;
import depromeet.lessonfour.server.food.infra.repository.FoodRepository;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import depromeet.lessonfour.server.toiletrecord.domain.repository.ToiletRecordRepository;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletColor;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletShape;
import depromeet.lessonfour.server.user.domain.entity.User;
import depromeet.lessonfour.server.user.domain.repository.UserRepository;
import io.restassured.RestAssured;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql(
    scripts = "/sql/cleanup.sql",
    config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED),
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class CalendarControllerE2ETest {

  @LocalServerPort private int port;

  @Autowired private JwtTokenGenerator jwtTokenGenerator;
  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private FoodRepository foodRepository;
  @Autowired private JpaActivityRecordRepository activityRecordRepository;
  @Autowired private ToiletRecordRepository toiletRecordRepository;

  private String validJwtToken;
  private Long testUserId;
  private User testUser;
  private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

    // 테스트 사용자 생성
    testUser = createTestUser("test@example.com", "password123", "testuser");
    testUserId = testUser.getId();
    validJwtToken = "Bearer " + jwtTokenGenerator.generateAccessToken(AccountContext.of(testUser));

    // 테스트용 Food 데이터 생성
    Food testFood = Food.builder().name("사과").score(4.5).build();
    foodRepository.save(testFood);
  }

  private User createTestUser(String email, String password, String nickname) {
    User user = User.register(email, nickname, passwordEncoder.encode(password));
    return userRepository.save(user);
  }

  private ActivityRecord createTestActivityRecord(LocalDateTime dateTime) {
    List<Food> foods = foodRepository.findAll();
    List<MealFood> mealFoods = List.of(new MealFood(MealTime.BREAKFAST, foods.getFirst()));

    return activityRecordRepository.save(
        ActivityRecord.createWithMeals(
            testUserId, 5, StressLevel.MEDIUM, ActivityAt.from(dateTime), mealFoods));
  }

  private ToiletRecord createTestToiletRecord(LocalDateTime dateTime) {
    ToiletRecord record =
        ToiletRecord.register(
            testUser,
            true,
            ToiletColor.DEFAULT,
            ToiletShape.BANANA,
            20,
            5,
            "test note",
            ActivityAt.from(dateTime));
    toiletRecordRepository.save(record);
    return record;
  }

  @Test
  @DisplayName("날짜 범위 내 활동 기록과 배변 기록 존재 여부를 조회한다")
  void givenDateRange_whenGetRecordExistence_thenReturnsList() {
    // Given
    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 5);

    // 1월 1일 - 활동 기록만
    createTestActivityRecord(LocalDateTime.of(2024, 1, 1, 10, 0));
    // 1월 2일 - 배변 기록만
    createTestToiletRecord(LocalDateTime.of(2024, 1, 2, 10, 0));
    // 1월 3일 - 둘 다
    createTestActivityRecord(LocalDateTime.of(2024, 1, 3, 10, 0));
    createTestToiletRecord(LocalDateTime.of(2024, 1, 3, 15, 0));
    // 1월 4일, 5일 - 기록 없음

    // When & Then
    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .queryParam("start", startDate.format(dateFormatter))
        .queryParam("end", endDate.format(dateFormatter))
        .when()
        .get("/api/v1/calendar")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.startDate", equalTo("2024-01-01"))
        .body("data.endDate", equalTo("2024-01-05")) // 요청한 범위의 endDate가 그대로 반환됨
        .body("data.results", hasSize(5)) // 요청한 날짜 범위 전체에 대한 결과 반환
        .body("data.results[0].date", equalTo("2024-01-01"))
        .body("data.results[0].activityExists", equalTo(true))
        .body("data.results[0].toiletExists", equalTo(false))
        .body("data.results[1].date", equalTo("2024-01-02"))
        .body("data.results[1].activityExists", equalTo(false))
        .body("data.results[1].toiletExists", equalTo(true))
        .body("data.results[2].date", equalTo("2024-01-03"))
        .body("data.results[2].activityExists", equalTo(true))
        .body("data.results[2].toiletExists", equalTo(true))
        .body("data.results[3].date", equalTo("2024-01-04"))
        .body("data.results[3].activityExists", equalTo(false))
        .body("data.results[3].toiletExists", equalTo(false))
        .body("data.results[4].date", equalTo("2024-01-05"))
        .body("data.results[4].activityExists", equalTo(false))
        .body("data.results[4].toiletExists", equalTo(false));
  }

  @Test
  @DisplayName("JWT 토큰 없이 조회 요청 시 401 에러를 반환한다")
  void givenNoAuthToken_whenGetRecordExistence_thenUnauthorized() {
    // Given
    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 5);

    // When & Then
    given()
        .log()
        .all()
        .queryParam("start", startDate.format(dateFormatter))
        .queryParam("end", endDate.format(dateFormatter))
        .when()
        .get("/api/v1/calendar")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  @DisplayName("유효하지 않은 JWT 토큰으로 조회 요청 시 401 에러를 반환한다")
  void givenInvalidAuthToken_whenGetRecordExistence_thenUnauthorized() {
    // Given
    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 5);

    // When & Then
    given()
        .log()
        .all()
        .header("Authorization", "Bearer invalid-token")
        .queryParam("start", startDate.format(dateFormatter))
        .queryParam("end", endDate.format(dateFormatter))
        .when()
        .get("/api/v1/calendar")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  @DisplayName("start 파라미터 없이 조회 요청 시 400 에러를 반환한다")
  void givenNoStartParameter_whenGetRecordExistence_thenBadRequest() {
    // Given
    LocalDate endDate = LocalDate.of(2024, 1, 5);

    // When & Then
    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .queryParam("end", endDate.format(dateFormatter))
        .when()
        .get("/api/v1/calendar")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  @DisplayName("유효하지 않은 날짜 형식으로 조회 요청 시 400 에러를 반환한다")
  void givenInvalidDateFormat_whenGetRecordExistence_thenBadRequest() {
    // When & Then
    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .queryParam("start", "invalid-date")
        .queryParam("end", "2024-01-05")
        .when()
        .get("/api/v1/calendar")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  @DisplayName("삭제된 기록은 조회되지 않는다")
  void givenDeletedRecords_whenGetRecordExistence_thenNotIncluded() {
    // Given
    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 2);

    // 1월 1일 - 삭제된 활동 기록
    ActivityRecord deletedActivityRecord =
        createTestActivityRecord(LocalDateTime.of(2024, 1, 1, 10, 0));
    deletedActivityRecord.delete();
    activityRecordRepository.save(deletedActivityRecord);

    // 1월 2일 - 삭제된 배변 기록
    ToiletRecord deletedToiletRecord = createTestToiletRecord(LocalDateTime.of(2024, 1, 2, 10, 0));
    deletedToiletRecord.delete();
    toiletRecordRepository.save(deletedToiletRecord);

    // When & Then
    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .queryParam("start", startDate.format(dateFormatter))
        .queryParam("end", endDate.format(dateFormatter))
        .when()
        .get("/api/v1/calendar")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.results", hasSize(2))
        .body("data.results[0].activityExists", equalTo(false))
        .body("data.results[0].toiletExists", equalTo(false))
        .body("data.results[1].activityExists", equalTo(false))
        .body("data.results[1].toiletExists", equalTo(false));
  }

  @Test
  @DisplayName("다른 사용자의 기록은 조회되지 않는다")
  void givenOtherUserRecords_whenGetRecordExistence_thenNotIncluded() {
    // Given
    User otherUser = createTestUser("other@example.com", "password456", "otheruser");
    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 2);

    // 다른 사용자의 기록 생성
    List<Food> foods = foodRepository.findAll();
    List<MealFood> mealFoods = List.of(new MealFood(MealTime.BREAKFAST, foods.get(0)));
    activityRecordRepository.save(
        ActivityRecord.createWithMeals(
            otherUser.getId(),
            5,
            StressLevel.MEDIUM,
            ActivityAt.from(LocalDateTime.of(2024, 1, 1, 10, 0)),
            mealFoods));

    ToiletRecord otherUserToiletRecord =
        ToiletRecord.register(
            otherUser,
            true,
            ToiletColor.DEFAULT,
            ToiletShape.BANANA,
            20,
            5,
            "test note",
            ActivityAt.from(LocalDateTime.of(2024, 1, 2, 10, 0)));
    toiletRecordRepository.save(otherUserToiletRecord);

    // When & Then - 현재 사용자로 조회 시 다른 사용자 기록은 보이지 않음
    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .queryParam("start", startDate.format(dateFormatter))
        .queryParam("end", endDate.format(dateFormatter))
        .when()
        .get("/api/v1/calendar")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.results", hasSize(2))
        .body("data.results[0].activityExists", equalTo(false))
        .body("data.results[0].toiletExists", equalTo(false))
        .body("data.results[1].activityExists", equalTo(false))
        .body("data.results[1].toiletExists", equalTo(false));
  }

  @Test
  @DisplayName("하루에 여러 기록이 있어도 존재 여부는 true로 표시된다")
  void givenMultipleRecordsPerDay_whenGetRecordExistence_thenShowsTrue() {
    // Given
    LocalDate targetDate = LocalDate.of(2024, 1, 1);

    // 같은 날 여러 활동 기록 (soft delete 제약조건 때문에 실제로는 하나만 가능하지만,
    // 시간대가 다르면 가능할 수 있음을 고려)
    createTestActivityRecord(LocalDateTime.of(2024, 1, 1, 10, 0));
    // 같은 날 여러 배변 기록
    createTestToiletRecord(LocalDateTime.of(2024, 1, 1, 12, 0));
    createTestToiletRecord(LocalDateTime.of(2024, 1, 1, 18, 0));

    // When & Then
    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .queryParam("start", targetDate.format(dateFormatter))
        .queryParam("end", targetDate.format(dateFormatter))
        .when()
        .get("/api/v1/calendar")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.results", hasSize(1))
        .body("data.results[0].date", equalTo("2024-01-01"))
        .body("data.results[0].activityExists", equalTo(true))
        .body("data.results[0].toiletExists", equalTo(true));
  }

  @Test
  @DisplayName("넓은 날짜 범위 조회 시 정상 동작한다")
  void givenWideDateRange_whenGetRecordExistence_thenSuccess() {
    // Given
    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 31); // 31일

    // 몇 개 날짜에만 기록 생성
    createTestActivityRecord(LocalDateTime.of(2024, 1, 1, 10, 0));
    createTestToiletRecord(LocalDateTime.of(2024, 1, 15, 10, 0));
    createTestActivityRecord(LocalDateTime.of(2024, 1, 31, 10, 0));

    // When & Then
    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .queryParam("start", startDate.format(dateFormatter))
        .queryParam("end", endDate.format(dateFormatter))
        .when()
        .get("/api/v1/calendar")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.startDate", equalTo("2024-01-01"))
        .body("data.endDate", equalTo("2024-01-31"))
        .body("data.results", hasSize(31)); // 요청한 날짜 범위 전체 반환
  }

  @Test
  @DisplayName("시작일만 있는 경우 정상 동작한다")
  void givenStartDate_whenGetRecordExistence_thenSuccess() {
    // Given
    LocalDate targetDate = LocalDate.of(2024, 1, 1);
    createTestActivityRecord(LocalDateTime.of(2024, 1, 1, 10, 0));

    // When & Then
    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .queryParam("start", targetDate.format(dateFormatter))
        .when()
        .get("/api/v1/calendar")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.startDate", equalTo("2024-01-01"))
        .body("data.endDate", equalTo("2024-01-01"))
        .body("data.results", hasSize(1))
        .body("data.results[0].date", equalTo("2024-01-01"))
        .body("data.results[0].activityExists", equalTo(true));
  }

  @Test
  @DisplayName("기록이 전혀 없는 날짜 범위 조회 시 빈 리스트를 반환한다")
  void givenNoRecords_whenGetRecordExistence_thenEmptyList() {
    // Given
    LocalDate startDate = LocalDate.of(2024, 12, 1);
    LocalDate endDate = LocalDate.of(2024, 12, 7);

    // When & Then
    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .queryParam("start", startDate.format(dateFormatter))
        .queryParam("end", endDate.format(dateFormatter))
        .when()
        .get("/api/v1/calendar")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.OK.value());
  }
}
