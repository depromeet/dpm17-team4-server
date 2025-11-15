package depromeet.lessonfour.server.report.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

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
class GetWeeklyReportE2ETest {

  @LocalServerPort private int port;

  @Autowired private JwtTokenGenerator jwtTokenGenerator;
  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private FoodRepository foodRepository;

  private String validJwtToken;
  private User testUser;
  private final DateTimeFormatter dateTimeFormatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

    testUser = createTestUser("weekly-report@example.com", "password123", "weekly-user");
    validJwtToken = "Bearer " + jwtTokenGenerator.generateAccessToken(AccountContext.of(testUser));

    // 최소 1개 식품 확보
    if (foodRepository.count() == 0) {
      foodRepository.save(Food.builder().name("사과").score(4.5).build());
    }
  }

  private User createTestUser(String email, String password, String nickname) {
    User user = User.register(email, nickname, passwordEncoder.encode(password));
    return userRepository.save(user);
  }

  private void createToilet(LocalDateTime dt, String color, String shape, int pain, int duration) {
    String createRequest =
        String.format(
            """
        {
          "occurredAt": "%s",
          "isSuccessful": true,
          "color": "%s",
          "shape": "%s",
          "pain": %d,
          "duration": %d,
          "note": "test note"
        }
        """,
            dt.format(dateTimeFormatter), color, shape, pain, duration);

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(createRequest)
        .when()
        .post("/api/v1/poo-records")
        .then()
        .statusCode(HttpStatus.OK.value());
  }

  private void createActivity(LocalDateTime dt) {
    List<Food> foods = foodRepository.findAll();
    Long foodId = foods.getFirst().getId();

    String createRequest =
        String.format(
            """
        {
          "foods": [{"id": %d, "mealTime": "BREAKFAST"}],
          "water": 5,
          "stress": "MEDIUM",
          "occurredAt": "%s"
        }
        """,
            foodId, dt.format(dateTimeFormatter));

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(createRequest)
        .when()
        .post("/api/v1/activity-records")
        .then()
        .statusCode(HttpStatus.CREATED.value());
  }

  @Test
  @DisplayName("[weekly] 배변 기록 2건 + 생활 기록 2건 → 정상 생성")
  void weeklyReport_success_with_2_toilets_and_2_activities() {
    // Given: 같은 주 내 서로 다른 날짜에 배변 기록 2건 + 생활 기록 2건
    // 2024-10-07 (월요일)부터 2024-10-13 (일요일)까지가 한 주
    LocalDateTime day1 = LocalDateTime.of(2024, 10, 7, 10, 0); // 월요일
    LocalDateTime day2 = LocalDateTime.of(2024, 10, 8, 14, 30); // 화요일 (다른 시간)

    // 배변 기록 2건 (서로 다른 날짜)
    createToilet(day1, "DEFAULT", "BANANA", 10, 5);
    createToilet(day2, "DEFAULT", "CORN", 15, 7);

    // 생활 기록 2건 (서로 다른 날짜)
    createActivity(day1);
    createActivity(day2);

    // 이전 주(2024-09-30 ~ 2024-10-06)에도 배변 기록 2건 필요
    LocalDateTime lastWeekDay1 = LocalDateTime.of(2024, 9, 30, 10, 0); // 월요일
    LocalDateTime lastWeekDay2 = LocalDateTime.of(2024, 10, 1, 10, 0); // 화요일
    createToilet(lastWeekDay1, "DEFAULT", "BANANA", 10, 5);
    createToilet(lastWeekDay2, "DEFAULT", "BANANA", 10, 5);

    // When & Then: 2024-10-07이 속한 주의 리포트 조회
    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .queryParam("dateTime", day1.format(dateTimeFormatter))
        .when()
        .get("/api/v1/reports/weekly")
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data", notNullValue())
        .body("data.defecationScore", notNullValue())
        .body("data.defecationScore.thisWeek", notNullValue())
        .body("data.defecationScore.lastWeek", notNullValue());
  }

  @Test
  @DisplayName("[weekly] 배변 기록 2건 + 생활 기록 0건 → 정상 생성")
  void weeklyReport_success_with_2_toilets_and_0_activities() {
    // Given: 같은 주 내 서로 다른 날짜에 배변 기록만 2건
    LocalDateTime baseDateTime = LocalDateTime.of(2024, 10, 7, 10, 0); // 월요일
    LocalDateTime day2 = LocalDateTime.of(2024, 10, 8, 10, 0); // 화요일

    createToilet(baseDateTime, "DEFAULT", "BANANA", 10, 5);
    createToilet(day2, "DEFAULT", "BANANA", 10, 5);

    // 이전 주에도 배변 기록 2건 필요
    LocalDateTime lastWeekDay1 = LocalDateTime.of(2024, 9, 30, 10, 0);
    LocalDateTime lastWeekDay2 = LocalDateTime.of(2024, 10, 1, 10, 0);
    createToilet(lastWeekDay1, "DEFAULT", "BANANA", 10, 5);
    createToilet(lastWeekDay2, "DEFAULT", "BANANA", 10, 5);

    // When & Then
    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .queryParam("dateTime", baseDateTime.format(dateTimeFormatter))
        .when()
        .get("/api/v1/reports/weekly")
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data", notNullValue())
        .body("data.defecationScore", notNullValue())
        .body("data.defecationScore.thisWeek", notNullValue())
        .body("data.defecationScore.lastWeek", notNullValue());
  }

  @Test
  @DisplayName("[weekly] 배변 기록 1건 + 생활 기록 2건 → 실패 (배변 기록 부족)")
  void weeklyReport_fail_with_1_toilet_and_2_activities() {
    // Given: 배변 기록 1건만 존재, 생활 기록은 2건
    LocalDateTime baseDateTime = LocalDateTime.of(2024, 10, 7, 10, 0); // 월요일
    LocalDateTime day2 = LocalDateTime.of(2024, 10, 8, 10, 0); // 화요일

    // 현재 주 배변 기록 1건만 (부족)
    createToilet(baseDateTime, "DEFAULT", "BANANA", 10, 5);

    createActivity(baseDateTime);
    createActivity(day2);

    // 이전 주에는 배변 기록 2건 필요 (이전 주는 정상이어야 현재 주만 실패 확인)
    LocalDateTime lastWeekDay1 = LocalDateTime.of(2024, 9, 30, 10, 0);
    LocalDateTime lastWeekDay2 = LocalDateTime.of(2024, 10, 1, 10, 0);
    createToilet(lastWeekDay1, "DEFAULT", "BANANA", 10, 5);
    createToilet(lastWeekDay2, "DEFAULT", "BANANA", 10, 5);

    // When & Then: 배변 기록이 2건 미만이므로 에러
    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .queryParam("dateTime", baseDateTime.format(dateTimeFormatter))
        .when()
        .get("/api/v1/reports/weekly")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .body("status", equalTo(400));
  }

  @Test
  @DisplayName("[weekly] 배변 기록 1건 + 생활 기록 0건 → 실패 (배변 기록 부족)")
  void weeklyReport_fail_with_1_toilet_and_0_activities() {
    // Given: 배변 기록 1건만 존재
    LocalDateTime baseDateTime = LocalDateTime.of(2024, 10, 7, 10, 0); // 월요일

    // 현재 주 배변 기록 1건만 (부족)
    createToilet(baseDateTime, "DEFAULT", "BANANA", 10, 5);

    // 이전 주에는 배변 기록 2건 필요
    LocalDateTime lastWeekDay1 = LocalDateTime.of(2024, 9, 30, 10, 0);
    LocalDateTime lastWeekDay2 = LocalDateTime.of(2024, 10, 1, 10, 0);
    createToilet(lastWeekDay1, "DEFAULT", "BANANA", 10, 5);
    createToilet(lastWeekDay2, "DEFAULT", "BANANA", 10, 5);

    // When & Then: 배변 기록이 2건 미만이므로 에러
    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .queryParam("dateTime", baseDateTime.format(dateTimeFormatter))
        .when()
        .get("/api/v1/reports/weekly")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .body("status", equalTo(400));
  }

  @Test
  @DisplayName("[weekly] 배변 기록 0건 + 생활 기록 0건 → 실패 (데이터 없음)")
  void weeklyReport_fail_with_0_toilets_and_0_activities() {
    // Given: 현재 주 데이터 없음, 이전 주에는 데이터 있음
    LocalDateTime baseDateTime = LocalDateTime.of(2024, 10, 7, 10, 0); // 월요일

    // 이전 주에는 배변 기록 2건 필요
    LocalDateTime lastWeekDay1 = LocalDateTime.of(2024, 9, 30, 10, 0);
    LocalDateTime lastWeekDay2 = LocalDateTime.of(2024, 10, 1, 10, 0);
    createToilet(lastWeekDay1, "DEFAULT", "BANANA", 10, 5);
    createToilet(lastWeekDay2, "DEFAULT", "BANANA", 10, 5);

    // When & Then: 배변 기록이 없으므로 에러
    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .queryParam("dateTime", baseDateTime.format(dateTimeFormatter))
        .when()
        .get("/api/v1/reports/weekly")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .body("status", equalTo(400));
  }
}
