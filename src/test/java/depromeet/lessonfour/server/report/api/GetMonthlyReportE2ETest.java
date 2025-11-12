package depromeet.lessonfour.server.report.api;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

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
class GetMonthlyReportE2ETest {

  @LocalServerPort private int port;

  @Autowired private JwtTokenGenerator jwtTokenGenerator;
  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private FoodRepository foodRepository;
  @Autowired private JpaToiletScoreRepository toiletScoreRepository;

  private String validJwtToken;
  private Long testUserId;
  private User testUser;
  private Long foodId;
  private final DateTimeFormatter formatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

    testUser = createTestUser("monthly-report@example.com", "password123", "monthly-user");
    testUserId = testUser.getId();
    validJwtToken = "Bearer " + jwtTokenGenerator.generateAccessToken(AccountContext.of(testUser));

    // 최소 1개 식품 확보
    Food food = foodRepository.save(Food.builder().name("사과").score(4.5).build());
    foodId = food.getId();
  }

  private User createTestUser(String email, String password, String nickname) {
    User user = User.register(email, nickname, passwordEncoder.encode(password));
    return userRepository.save(user);
  }

  private void createActivityViaAPI(LocalDateTime dateTime) {
    String createRequest =
        String.format(
            """
        {
          "foods": [
            {
              "id": %d,
              "mealTime": "BREAKFAST"
            }
          ],
          "water": 5,
          "stress": "MEDIUM",
          "occurredAt": "%s"
        }
        """,
            foodId, dateTime.format(formatter));

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(createRequest)
        .when()
        .post("/api/v1/activity-records")
        .then()
        .statusCode(HttpStatus.CREATED.value());
  }

  private void createToiletViaAPI(
      LocalDateTime dateTime,
      Boolean isSuccessful,
      String color,
      String shape,
      Integer pain,
      Integer duration,
      String note) {
    // Default values
    boolean successValue = isSuccessful != null ? isSuccessful : true;
    String colorValue = color != null ? color : "DEFAULT";
    String shapeValue = shape != null ? shape : "BANANA";
    int painValue = pain != null ? pain : 0;
    int durationValue = duration != null ? duration : 10;
    String noteValue = note != null ? note : "";

    String createRequest =
        String.format(
            """
        {
          "occurredAt": "%s",
          "isSuccessful": %s,
          "color": "%s",
          "shape": "%s",
          "pain": %d,
          "duration": %d,
          "note": "%s"
        }
        """,
            dateTime.format(formatter),
            successValue,
            colorValue,
            shapeValue,
            painValue,
            durationValue,
            noteValue);

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", validJwtToken)
        .body(createRequest)
        .when()
        .post("/api/v1/poo-records")
        .then()
        .statusCode(HttpStatus.OK.value());

    // ToiletScore 생성 대기
    await()
        .atMost(Duration.ofSeconds(5))
        .pollInterval(Duration.ofMillis(100))
        .untilAsserted(
            () -> {
              boolean exists =
                  toiletScoreRepository
                      .findByUserIdAndDate(testUserId, dateTime.toLocalDate())
                      .isPresent();
              if (!exists) {
                throw new AssertionError("ToiletScore not created yet");
              }
            });
  }

  private String monthlyUrl(YearMonth yearMonth) {
    return "/api/v1/reports/monthly?yearMonth=" + yearMonth.toString();
  }

  // 1) 정상 케이스 테스트: 2개 이상의 주차에 각각 2일 이상 배변 기록
  @Test
  @DisplayName("[E2E][monthly] 정상 케이스 - 2개 주차에 각각 배변 기록 2일 이상 → 월간 리포트 생성 성공")
  void givenTwoWeeksWithSufficientToiletRecords_whenGetMonthlyReport_thenSuccess() {
    // Given: 2024년 1월
    // 1주차: 1일, 2일, 3일 배변 기록
    // 2주차: 8일, 9일, 10일 배변 기록
    YearMonth targetMonth = YearMonth.of(2024, 1);

    // 1주차 배변 기록 (1일, 2일, 3일) - API 호출로 생성
    createToiletViaAPI(
        LocalDateTime.of(2024, 1, 1, 10, 0), true, "DEFAULT", "BANANA", 10, 5, "1주차 1일");
    createToiletViaAPI(LocalDateTime.of(2024, 1, 2, 10, 0), true, "GOLD", "CORN", 15, 6, "1주차 2일");
    createToiletViaAPI(
        LocalDateTime.of(2024, 1, 3, 10, 0), true, "DARK_BROWN", "CREAM", 12, 7, "1주차 3일");

    // 2주차 배변 기록 (8일, 9일, 10일) - API 호출로 생성
    createToiletViaAPI(
        LocalDateTime.of(2024, 1, 8, 10, 0), true, "DEFAULT", "BANANA", 8, 5, "2주차 8일");
    createToiletViaAPI(LocalDateTime.of(2024, 1, 9, 10, 0), true, "GOLD", "CORN", 10, 6, "2주차 9일");
    createToiletViaAPI(
        LocalDateTime.of(2024, 1, 10, 10, 0), true, "DARK_BROWN", "CREAM", 15, 7, "2주차 10일");

    // When & Then
    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(monthlyUrl(targetMonth))
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(201))
        .body("data.monthlyRecordCounts", notNullValue())
        .body("data.monthlyDefecationScore", notNullValue())
        .body("data.userAverage", notNullValue())
        .body("data.monthlyScore", notNullValue())
        .body("data.shape", notNullValue())
        .body("data.timeDistribution", notNullValue())
        .body("data.color", notNullValue())
        .body("data.pain", notNullValue())
        .body("data.timeOfDay", notNullValue())
        .body("data.suggestion", notNullValue());
  }

  // 2) 주간 기록이 한 건 있어서 실패하는 케이스
  @Test
  @DisplayName("[E2E][monthly] 주간 리포트 1개만 존재 → 데이터 부족으로 월간 리포트 생성 실패")
  void givenOnlyOneWeekWithToiletRecords_whenGetMonthlyReport_thenFail() {
    // Given: 2024년 2월
    // 1주차에만 배변 기록 3일
    YearMonth targetMonth = YearMonth.of(2024, 2);

    createToiletViaAPI(
        LocalDateTime.of(2024, 2, 1, 10, 0), true, "DEFAULT", "BANANA", 10, 5, "1주차 1일");
    createToiletViaAPI(LocalDateTime.of(2024, 2, 2, 10, 0), true, "GOLD", "CORN", 15, 6, "1주차 2일");
    createToiletViaAPI(
        LocalDateTime.of(2024, 2, 3, 10, 0), true, "DARK_BROWN", "CREAM", 12, 7, "1주차 3일");

    // When & Then
    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(monthlyUrl(targetMonth))
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(400));
  }

  // 3) Suggestion을 최대 3개까지 응답하는지 테스트
  @Test
  @DisplayName("[E2E][monthly] Suggestion 섹션에 최대 3개까지 추천 습관 반환")
  void givenSufficientData_whenGetMonthlyReport_thenSuggestionHasUpToThreeItems() {
    // Given: 2024년 3월
    // 다양한 패턴의 배변 및 생활 기록 생성하여 suggestion이 생성되도록 함
    YearMonth targetMonth = YearMonth.of(2024, 3);

    // 1주차: 4일 연속 배변 기록 (충분한 데이터)
    for (int i = 1; i <= 4; i++) {
      LocalDateTime day = LocalDateTime.of(2024, 3, i, 10, 0);
      createToiletViaAPI(day, true, "DEFAULT", "BANANA", 10, 5, "1주차 " + i + "일");
      createActivityViaAPI(day);
    }

    // 2주차: 4일 배변 기록 (충분한 데이터)
    for (int i = 8; i <= 11; i++) {
      LocalDateTime day = LocalDateTime.of(2024, 3, i, 10, 0);
      createToiletViaAPI(day, true, "GOLD", "CORN", 15, 6, "2주차 " + i + "일");
      createActivityViaAPI(day);
    }

    // 3주차: 3일 배변 기록 (추가 주차)
    for (int i = 15; i <= 17; i++) {
      LocalDateTime day = LocalDateTime.of(2024, 3, i, 10, 0);
      createToiletViaAPI(day, true, "DARK_BROWN", "CREAM", 20, 7, "3주차 " + i + "일");
      createActivityViaAPI(day);
    }

    // When & Then
    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(monthlyUrl(targetMonth))
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(201))
        .body("data.suggestion", notNullValue())
        .body("data.suggestion.items.size()", greaterThanOrEqualTo(0)) // suggestion은 0개 이상
        .body("data.suggestion.items.size()", lessThanOrEqualTo(3)); // 최대 3개까지
  }

  // 4) 배변 기록은 충족되지만 생활 기록이 없는 경우 => 성공
  @Test
  @DisplayName("[E2E][monthly] 배변 기록만 충족, 생활 기록 없음 → 월간 리포트 생성 성공")
  void givenOnlyToiletRecordsWithoutActivity_whenGetMonthlyReport_thenSuccess() {
    // Given: 2024년 4월
    // 배변 기록만 있고 생활 기록은 없음
    YearMonth targetMonth = YearMonth.of(2024, 4);

    // 1주차: 1일, 2일, 3일 배변 기록
    createToiletViaAPI(
        LocalDateTime.of(2024, 4, 1, 10, 0), true, "DEFAULT", "BANANA", 10, 5, "1주차 1일");
    createToiletViaAPI(LocalDateTime.of(2024, 4, 2, 10, 0), true, "GOLD", "CORN", 12, 6, "1주차 2일");
    createToiletViaAPI(
        LocalDateTime.of(2024, 4, 3, 10, 0), true, "DARK_BROWN", "CREAM", 15, 7, "1주차 3일");

    // 2주차: 8일, 9일, 10일 배변 기록
    createToiletViaAPI(
        LocalDateTime.of(2024, 4, 8, 10, 0), true, "DEFAULT", "BANANA", 8, 5, "2주차 8일");
    createToiletViaAPI(LocalDateTime.of(2024, 4, 9, 10, 0), true, "GOLD", "CORN", 10, 6, "2주차 9일");
    createToiletViaAPI(
        LocalDateTime.of(2024, 4, 10, 10, 0), true, "DARK_BROWN", "CREAM", 12, 7, "2주차 10일");

    // 생활 기록은 생성하지 않음

    // When & Then
    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(monthlyUrl(targetMonth))
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(201))
        .body("data.monthlyRecordCounts", notNullValue())
        .body("data.monthlyDefecationScore", notNullValue())
        .body("data.userAverage", notNullValue());
  }

  // 5) 생활 기록은 충족되지만 배변 기록이 부족한 경우 => 실패
  @Test
  @DisplayName("[E2E][monthly] 생활 기록 충분하지만 배변 기록 부족 (1주차만 배변 기록) → 월간 리포트 생성 실패")
  void givenSufficientActivityButInsufficientToilet_whenGetMonthlyReport_thenFail() {
    // Given: 2024년 5월
    // 생활 기록은 많지만 배변 기록은 1주차에만 존재
    YearMonth targetMonth = YearMonth.of(2024, 5);

    // 1주차: 배변 기록 3일 + 생활 기록 7일
    for (int i = 1; i <= 7; i++) {
      LocalDateTime day = LocalDateTime.of(2024, 5, i, 10, 0);
      createActivityViaAPI(day); // 생활 기록은 매일
      if (i <= 3) {
        // 배변 기록은 1, 2, 3일만
        createToiletViaAPI(day, true, "DEFAULT", "BANANA", 10, 5, "1주차 " + i + "일");
      }
    }

    // 2주차: 생활 기록만 7일 (배변 기록 없음)
    for (int i = 8; i <= 14; i++) {
      LocalDateTime day = LocalDateTime.of(2024, 5, i, 10, 0);
      createActivityViaAPI(day); // 생활 기록만
      // 배변 기록 없음
    }

    // When & Then
    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(monthlyUrl(targetMonth))
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(400));
  }
}
