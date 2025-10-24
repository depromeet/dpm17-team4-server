package depromeet.lessonfour.server.recordquery.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

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
import org.springframework.jdbc.core.JdbcTemplate;
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
public class GetDailyRecordE2ETest {

  @LocalServerPort private int port;

  @Autowired private JwtTokenGenerator jwtTokenGenerator;
  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private FoodRepository foodRepository;
  @Autowired private JpaActivityRecordRepository activityRecordRepository;
  @Autowired private ToiletRecordRepository toiletRecordRepository;
  @Autowired private JdbcTemplate jdbcTemplate;

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

  private void createToiletScore(Long userId, LocalDate date, int score) {
    jdbcTemplate.update(
        "INSERT INTO toilet_score (user_id, date, score) VALUES (?, ?, ?)", userId, date, score);
  }

  @Test
  @DisplayName("특정 날짜의 일일 기록을 조회한다")
  void givenDailyRecordData_whenGetDailyRecord_thenReturnsDailyRecord() {
    // Given
    LocalDate targetDate = LocalDate.of(2024, 1, 15);

    // 활동 기록 생성
    createTestActivityRecord(LocalDateTime.of(2024, 1, 15, 10, 0));

    // 배변 기록 2개 생성
    createTestToiletRecord(LocalDateTime.of(2024, 1, 15, 8, 0));
    createTestToiletRecord(LocalDateTime.of(2024, 1, 15, 14, 0));

    // ToiletScore 생성
    createToiletScore(testUserId, targetDate, 75);

    // When & Then
    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .when()
        .get("/api/v1/calendar/" + targetDate.format(dateFormatter))
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.score", equalTo(75))
        .body("data.toiletRecordCount", equalTo(2))
        .body("data.hasActivityRecord", equalTo(true));
  }

  @Test
  @DisplayName("기록이 없는 날짜의 일일 기록 조회 시 배변 기록 카운트가 0이고 생활 기록 존재 여부가 false로 반환된다")
  void givenNoRecordData_whenGetDailyRecord_thenReturnsZeroValues() {
    // Given
    LocalDate targetDate = LocalDate.of(2024, 1, 15);

    // When & Then
    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .when()
        .get("/api/v1/calendar/" + targetDate.format(dateFormatter))
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.score", equalTo(0))
        .body("data.toiletRecordCount", equalTo(0))
        .body("data.hasActivityRecord", equalTo(false));
  }

  @Test
  @DisplayName("JWT 토큰 없이 일일 기록 조회 요청 시 401 에러를 반환한다")
  void givenNoAuthToken_whenGetDailyRecord_thenUnauthorized() {
    // Given
    LocalDate targetDate = LocalDate.of(2024, 1, 15);

    // When & Then
    given()
        .log()
        .all()
        .when()
        .get("/api/v1/calendar/" + targetDate.format(dateFormatter))
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  @DisplayName("유효하지 않은 JWT 토큰으로 일일 기록 조회 요청 시 401 에러를 반환한다")
  void givenInvalidAuthToken_whenGetDailyRecord_thenUnauthorized() {
    // Given
    LocalDate targetDate = LocalDate.of(2024, 1, 15);

    // When & Then
    given()
        .log()
        .all()
        .header("Authorization", "Bearer invalid-token")
        .when()
        .get("/api/v1/calendar/" + targetDate.format(dateFormatter))
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  @DisplayName("유효하지 않은 날짜 형식으로 일일 기록 조회 요청 시 400 에러를 반환한다")
  void givenInvalidDateFormat_whenGetDailyRecord_thenBadRequest() {
    // When & Then
    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .when()
        .get("/api/v1/calendar/invalid-date")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  @DisplayName("삭제된 기록은 일일 기록 카운트에 포함되지 않는다")
  void givenDeletedRecords_whenGetDailyRecord_thenNotCounted() {
    // Given
    LocalDate targetDate = LocalDate.of(2024, 1, 15);

    // 삭제된 활동 기록
    ActivityRecord deletedActivityRecord =
        createTestActivityRecord(LocalDateTime.of(2024, 1, 15, 10, 0));
    deletedActivityRecord.delete();
    activityRecordRepository.save(deletedActivityRecord);

    // 삭제된 배변 기록
    ToiletRecord deletedToiletRecord = createTestToiletRecord(LocalDateTime.of(2024, 1, 15, 8, 0));
    deletedToiletRecord.delete();
    toiletRecordRepository.save(deletedToiletRecord);

    // When & Then
    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .when()
        .get("/api/v1/calendar/" + targetDate.format(dateFormatter))
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.toiletRecordCount", equalTo(0))
        .body("data.hasActivityRecord", equalTo(false));
  }

  @Test
  @DisplayName("다른 사용자의 기록은 일일 기록 카운트에 포함되지 않는다")
  void givenOtherUserRecords_whenGetDailyRecord_thenNotCounted() {
    // Given
    User otherUser = createTestUser("other@example.com", "password456", "otheruser");
    LocalDate targetDate = LocalDate.of(2024, 1, 15);

    // 다른 사용자의 활동 기록 생성
    List<Food> foods = foodRepository.findAll();
    List<MealFood> mealFoods = List.of(new MealFood(MealTime.BREAKFAST, foods.get(0)));
    activityRecordRepository.save(
        ActivityRecord.createWithMeals(
            otherUser.getId(),
            5,
            StressLevel.MEDIUM,
            ActivityAt.from(LocalDateTime.of(2024, 1, 15, 10, 0)),
            mealFoods));

    // 다른 사용자의 배변 기록 생성
    ToiletRecord otherUserToiletRecord =
        ToiletRecord.register(
            otherUser,
            true,
            ToiletColor.DEFAULT,
            ToiletShape.BANANA,
            20,
            5,
            "test note",
            ActivityAt.from(LocalDateTime.of(2024, 1, 15, 14, 0)));
    toiletRecordRepository.save(otherUserToiletRecord);

    // 다른 사용자의 점수 생성
    createToiletScore(otherUser.getId(), targetDate, 80);

    // When & Then - 현재 사용자로 조회 시 다른 사용자 기록은 카운트되지 않음
    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .when()
        .get("/api/v1/calendar/" + targetDate.format(dateFormatter))
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.score", equalTo(0))
        .body("data.toiletRecordCount", equalTo(0))
        .body("data.hasActivityRecord", equalTo(false));
  }

  @Test
  @DisplayName("하루에 여러 배변 기록이 있는 경우 모두 카운트된다")
  void givenMultipleToiletRecords_whenGetDailyRecord_thenCountsAll() {
    // Given
    LocalDate targetDate = LocalDate.of(2024, 1, 15);

    // 같은 날 여러 배변 기록
    createTestToiletRecord(LocalDateTime.of(2024, 1, 15, 8, 0));
    createTestToiletRecord(LocalDateTime.of(2024, 1, 15, 12, 0));
    createTestToiletRecord(LocalDateTime.of(2024, 1, 15, 18, 0));

    createToiletScore(testUserId, targetDate, 90);

    // When & Then
    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .when()
        .get("/api/v1/calendar/" + targetDate.format(dateFormatter))
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.score", equalTo(90))
        .body("data.toiletRecordCount", equalTo(3));
  }

  @Test
  @DisplayName("점수만 있고 기록이 없는 경우 점수만 반환된다")
  void givenOnlyScore_whenGetDailyRecord_thenReturnsScoreOnly() {
    // Given
    LocalDate targetDate = LocalDate.of(2024, 1, 15);
    createToiletScore(testUserId, targetDate, 50);

    // When & Then
    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .when()
        .get("/api/v1/calendar/" + targetDate.format(dateFormatter))
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.score", equalTo(50))
        .body("data.toiletRecordCount", equalTo(0))
        .body("data.hasActivityRecord", equalTo(false));
  }

  @Test
  @DisplayName("최대 점수 100점인 일일 기록이 정상 조회된다")
  void givenMaxScore_whenGetDailyRecord_thenSuccess() {
    // Given
    LocalDate targetDate = LocalDate.of(2024, 1, 15);
    createTestActivityRecord(LocalDateTime.of(2024, 1, 15, 10, 0));
    createTestToiletRecord(LocalDateTime.of(2024, 1, 15, 8, 0));
    createToiletScore(testUserId, targetDate, 100);

    // When & Then
    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .when()
        .get("/api/v1/calendar/" + targetDate.format(dateFormatter))
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.score", equalTo(100))
        .body("data.toiletRecordCount", equalTo(1))
        .body("data.hasActivityRecord", equalTo(true));
  }
}
