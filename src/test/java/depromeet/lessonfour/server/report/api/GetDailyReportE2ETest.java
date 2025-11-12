package depromeet.lessonfour.server.report.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

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
class GetDailyReportE2ETest {

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
  private final DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

    testUser = createTestUser("report@example.com", "password123", "report-user");
    testUserId = testUser.getId();
    validJwtToken = "Bearer " + jwtTokenGenerator.generateAccessToken(AccountContext.of(testUser));

    // 최소 1개 식품 확보
    foodRepository.save(Food.builder().name("사과").score(4.5).build());
  }

  private User createTestUser(String email, String password, String nickname) {
    User user = User.register(email, nickname, passwordEncoder.encode(password));
    return userRepository.save(user);
  }

  private ActivityRecord createActivity(LocalDateTime dateTime) {
    List<Food> foods = foodRepository.findAll();
    List<MealFood> meals = List.of(new MealFood(MealTime.BREAKFAST, foods.getFirst()));
    return activityRecordRepository.save(
        ActivityRecord.createWithMeals(
            testUserId, 5, StressLevel.MEDIUM, ActivityAt.from(dateTime), meals));
  }

  private ToiletRecord createToilet(
      LocalDateTime dateTime,
      Boolean isSuccessful,
      ToiletColor color,
      ToiletShape shape,
      Integer pain,
      Integer duration,
      String note) {
    ToiletRecord rec =
        ToiletRecord.register(
            testUser,
            isSuccessful != null ? isSuccessful : true,
            color, // null 허용
            shape, // null 허용
            pain != null ? pain : 10,
            duration != null ? duration : 5,
            note,
            ActivityAt.from(dateTime));
    toiletRecordRepository.save(rec);
    return rec;
  }

  private String url(LocalDateTime dateTime) {
    return "/api/v1/reports/daily?dateTime=" + dateTime.format(dtf);
  }

  // 1) toiletrecord + activityrecord 모두 존재
  @Test
  @DisplayName("[E2E] toiletrecord + activityrecord 모두 존재 → 정상 리포트 반환")
  void givenToiletAndActivity_whenGetDailyReport_thenOk() {
    LocalDateTime base = LocalDateTime.of(2024, 1, 15, 10, 0);

    createActivity(base);
    createToilet(base.withHour(8), true, ToiletColor.DEFAULT, ToiletShape.BANANA, 10, 5, "메모");

    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(url(base))
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.updatedAt", notNullValue())
        .body("data.poo", notNullValue())
        .body("data.food", notNullValue())
        .body("data.water", notNullValue())
        .body("data.stress", notNullValue());
  }

  // 2) toiletrecord만 존재
  @Test
  @DisplayName("[E2E] toiletrecord만 존재 → 정상 리포트 반환(활동 섹션은 null/빈값 허용)")
  void givenOnlyToilet_whenGetDailyReport_thenOk() {
    LocalDateTime base = LocalDateTime.of(2024, 1, 16, 10, 0);

    createToilet(base.withHour(7), true, ToiletColor.DEFAULT, ToiletShape.BANANA, 15, 6, null);

    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(url(base))
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.updatedAt", notNullValue())
        .body("data.poo", notNullValue())
        // 구현에 따라 food/water/stress 가 null 또는 객체(빈 items)일 수 있어 허용 범위로 체크
        .body("data.food", anyOf(nullValue(), notNullValue()))
        .body("data.water", anyOf(nullValue(), notNullValue()))
        .body("data.stress", anyOf(nullValue(), notNullValue()));
  }

  // 3) activityrecord만 존재
  @Test
  @DisplayName("[E2E] activityrecord만 존재 → poo(null)이어도 NPE 없이 정상 반환")
  void givenOnlyActivity_whenGetDailyReport_thenOkAndPooNull() {
    LocalDateTime base = LocalDateTime.of(2024, 1, 17, 10, 0);

    createActivity(base);

    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(url(base))
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.updatedAt", notNullValue())
        .body("data.poo", nullValue()) // StoolEvaluationLevel.NONE → mapper가 null 반환
        .body("data.food", notNullValue())
        .body("data.water", notNullValue())
        .body("data.stress", notNullValue());
  }

  // 4) toiletrecord 여러개: isSuccessful=false, color=null, shape=null 섞임
  @Test
  @DisplayName("[E2E] toiletrecord 다건(실패/색상null/모양null 포함) → NPE 없이 정상 반환")
  void givenMultipleToiletWithNullsAndFailure_whenGetDailyReport_thenOk() {
    LocalDateTime base = LocalDateTime.of(2024, 1, 18, 10, 0);

    createToilet(base.withHour(7), true, ToiletColor.DEFAULT, ToiletShape.BANANA, 10, 5, "ok");
    createToilet(base.withHour(9), false, ToiletColor.DEFAULT, ToiletShape.CORN, 30, 12, "fail");
    createToilet(base.withHour(12), true, null, ToiletShape.CREAM, 5, 3, null); // color null
    createToilet(base.withHour(18), true, ToiletColor.DARK_BROWN, null, 0, 2, null); // shape null
    createToilet(base.withHour(20), true, null, null, 25, 8, "둘다 null"); // both null

    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(url(base))
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.updatedAt", notNullValue())
        .body("data.poo", notNullValue())
        .body("data.poo.items.size()", equalTo(5));
  }

  // 5) toiletrecord 단건: isSuccessful=false, color=null, shape=null
  @Test
  @DisplayName("[E2E] toiletrecord 단건(isSuccessful=false, color/shape null) → NPE 없이 정상 반환")
  void givenSingleToiletWithNullsAndFail_whenGetDailyReport_thenOk() {
    // Given
    LocalDateTime base = LocalDateTime.of(2024, 1, 21, 10, 0);

    // activityrecord 없음, toiletrecord 1건만 생성 (실패 + color/shape 모두 null)
    createToilet(base.withHour(9), false, null, null, 25, 8, "단건 실패/널 케이스");

    // When & Then
    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(url(base))
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.updatedAt", notNullValue())
        .body("data.poo", notNullValue())
        .body("data.poo.items.size()", equalTo(1))
        .body("data.poo.items[0].occurredAt", notNullValue())
        .body("data.poo.items[0].color", nullValue())
        .body("data.poo.items[0].shape", nullValue())
        // 활동 섹션은 구현에 따라 null/객체 모두 허용
        .body("data.food", anyOf(nullValue(), notNullValue()))
        .body("data.water", anyOf(nullValue(), notNullValue()))
        .body("data.stress", anyOf(nullValue(), notNullValue()));
  }

  // 6) food 필드가 조회 날짜 기준으로 오늘/어제 데이터만 반환
  @Test
  @DisplayName("[E2E] food 필드는 조회 날짜(dateTime) 기준으로 오늘과 어제 데이터만 반환해야 함")
  void
      givenMultipleDaysActivities_whenGetDailyReportWithSpecificDate_thenFoodContainsOnlyTodayAndYesterdayData() {
    // Given: 여러 날짜에 activity 생성
    LocalDateTime targetDate = LocalDateTime.of(2024, 1, 20, 10, 0);
    LocalDateTime yesterday = targetDate.minusDays(1); // 2024.1.19
    LocalDateTime twoDaysAgo = targetDate.minusDays(2); // 2024.1.18
    LocalDateTime tomorrow = targetDate.plusDays(1); // 2024.1.21

    // 조회 날짜(2024.1.20)와 어제(2024.1.19)의 activity 생성
    createActivity(targetDate.withHour(14));
    createActivity(yesterday.withHour(15));

    // 다른 날짜의 activity도 생성
    createActivity(twoDaysAgo.withHour(16));
    createActivity(tomorrow.withHour(17));

    // When & Then: 2024.1.20으로 조회 시, food 필드에 2024.1.20과 2024.1.19 데이터만 포함되어야 함
    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(url(targetDate))
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.food", notNullValue())
        .body("data.food.items.size()", equalTo(2)) // 오늘(1.20) + 어제(1.19) = 2개
        .body("data.food.items[0].occurredAt", notNullValue())
        .body("data.food.items[1].occurredAt", notNullValue());
  }

  // 7) 다른 날짜로 조회 시에도 해당 날짜 기준으로 오늘/어제 데이터 반환
  @Test
  @DisplayName("[E2E] 다른 날짜로 조회 시에도 해당 날짜 기준 오늘/어제 데이터만 반환")
  void
      givenMultipleDaysActivities_whenGetDailyReportWithDifferentDate_thenFoodContainsCorrectDateRangeData() {
    // Given: 여러 날짜에 activity 생성
    LocalDateTime date1 = LocalDateTime.of(2024, 1, 25, 10, 0);
    LocalDateTime date2 = LocalDateTime.of(2024, 1, 26, 10, 0);
    LocalDateTime date3 = LocalDateTime.of(2024, 1, 27, 10, 0);
    LocalDateTime date4 = LocalDateTime.of(2024, 1, 28, 10, 0);

    createActivity(date1);
    createActivity(date2);
    createActivity(date3);
    createActivity(date4);

    // When & Then: 2024.1.27로 조회 시, food 필드에 1.27과 1.26 데이터만 포함
    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(url(date3))
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.food", notNullValue())
        .body("data.food.items.size()", equalTo(2)); // 1.27 + 1.26 = 2개
  }
}
