package depromeet.lessonfour.server.report.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

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
class GetWeeklyReportE2ETest {

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

    testUser = createTestUser("weekly-report@example.com", "password123", "weekly-user");
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
            color,
            shape,
            pain != null ? pain : 10,
            duration != null ? duration : 5,
            note,
            ActivityAt.from(dateTime));
    toiletRecordRepository.save(rec);
    return rec;
  }

  private String weeklyUrl(LocalDateTime dateTime) {
    return "/api/v1/reports/weekly?dateTime=" + dateTime.format(dtf);
  }

  // 1) 7일 모두 activity + toilet 존재
  @Test
  @DisplayName("[E2E][weekly] 7일 모두 activity + toilet 존재 → 정상 리포트 반환")
  void givenFullWeekActivityAndToilet_whenGetWeeklyReport_thenOk() {
    // 주간 기준: 2024-01-15(월) ~ 2024-01-21(일)
    LocalDateTime monday = LocalDateTime.of(2024, 1, 15, 10, 0);

    // 월~일 모두 activity + toilet 생성
    for (int i = 0; i < 7; i++) {
      LocalDateTime day = monday.plusDays(i);
      createActivity(day);
      createToilet(day.withHour(8), true, ToiletColor.DEFAULT, ToiletShape.BANANA, 10, 5, "메모");
    }

    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(weeklyUrl(monday.plusDays(3))) // 주 중 아무 날짜
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.updatedAt", notNullValue())
        .body("data.defecationScore", notNullValue())
        .body("data.defecationScore.dailyScore.size()", equalTo(7))
        .body("data.userAverage", notNullValue())
        .body("data.food", notNullValue())
        .body("data.water", notNullValue())
        .body("data.stress", notNullValue())
        .body("data.suggestion", notNullValue());
  }

  // 2) 일부 activity 없음 (toilet은 7일 모두 존재)
  @Test
  @DisplayName("[E2E][weekly] 일부 activity 없음 → NPE 없이 정상 리포트 반환")
  void givenPartialActivityMissing_whenGetWeeklyReport_thenOk() {
    LocalDateTime monday = LocalDateTime.of(2024, 1, 22, 10, 0);

    // 월~일: toilet은 모두 생성
    for (int i = 0; i < 7; i++) {
      LocalDateTime day = monday.plusDays(i);
      createToilet(day.withHour(9), true, ToiletColor.DEFAULT, ToiletShape.BANANA, 10, 5, null);
    }

    // activity는 월/수/금만 생성
    createActivity(monday.plusDays(0)); // 월
    createActivity(monday.plusDays(2)); // 수
    createActivity(monday.plusDays(4)); // 금

    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(weeklyUrl(monday.plusDays(3)))
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.updatedAt", notNullValue())
        .body("data.defecationScore", notNullValue())
        .body("data.defecationScore.dailyScore.size()", equalTo(7))
        .body("data.userAverage", notNullValue())
        .body("data.food", notNullValue())
        .body("data.water", notNullValue())
        .body("data.stress", notNullValue())
        .body("data.suggestion", notNullValue());
  }

  // 3) 일부 toilet 없음 (activity는 7일 모두 존재)
  @Test
  @DisplayName("[E2E][weekly] 일부 toilet 없음 → NPE 없이 정상 리포트 반환")
  void givenPartialToiletMissing_whenGetWeeklyReport_thenOk() {
    LocalDateTime monday = LocalDateTime.of(2024, 1, 29, 10, 0);

    // 월~일: activity 모두 생성
    for (int i = 0; i < 7; i++) {
      createActivity(monday.plusDays(i));
    }

    // toilet은 화/목/토만 생성
    createToilet(
        monday.plusDays(1).withHour(8),
        true,
        ToiletColor.DEFAULT,
        ToiletShape.BANANA,
        10,
        5,
        null); // 화
    createToilet(
        monday.plusDays(3).withHour(8),
        true,
        ToiletColor.DEFAULT,
        ToiletShape.CORN,
        20,
        8,
        null); // 목
    createToilet(
        monday.plusDays(5).withHour(8),
        true,
        ToiletColor.DARK_BROWN,
        ToiletShape.CREAM,
        5,
        3,
        null); // 토

    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(weeklyUrl(monday.plusDays(4)))
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.updatedAt", notNullValue())
        .body("data.defecationScore", notNullValue())
        .body("data.defecationScore.dailyScore.size()", equalTo(7))
        .body("data.userAverage", notNullValue())
        .body("data.food", notNullValue())
        .body("data.water", notNullValue())
        .body("data.stress", notNullValue())
        .body("data.suggestion", notNullValue());
  }

  // 4) 일부 날짜는 activity + toilet 둘 다 없음 (중간에 구멍)
  @Test
  @DisplayName("[E2E][weekly] 일부 날짜에 activity/toilet 둘 다 없음 → 정상 리포트 반환")
  void givenSomeDaysNoActivityAndToilet_whenGetWeeklyReport_thenOk() {
    LocalDateTime monday = LocalDateTime.of(2024, 2, 5, 10, 0);

    // 월/화: activity + toilet 있음
    for (int i = 0; i < 2; i++) {
      LocalDateTime day = monday.plusDays(i);
      createActivity(day);
      createToilet(day.withHour(7), true, ToiletColor.DEFAULT, ToiletShape.BANANA, 10, 5, null);
    }

    // 수/목: 아무 기록 없음

    // 금: activity만 있음
    createActivity(monday.plusDays(4));

    // 토: toilet만 있음
    createToilet(
        monday.plusDays(5).withHour(9), true, ToiletColor.GOLD, ToiletShape.CORN, 15, 6, null);

    // 일: 아무 기록 없음

    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(weeklyUrl(monday.plusDays(3)))
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.updatedAt", notNullValue())
        .body("data.defecationScore", notNullValue())
        .body("data.defecationScore.dailyScore.size()", equalTo(7))
        .body("data.userAverage", notNullValue())
        .body("data.food", notNullValue())
        .body("data.water", notNullValue())
        .body("data.stress", notNullValue())
        .body("data.suggestion", notNullValue());
  }

  // 5) 해당 주간에 activity, toilet 모두 없음
  @Test
  @DisplayName("[E2E][weekly] 해당 주간에 어떤 기록도 없음 → 리포트 생성 불가")
  void givenNoRecords_whenGetWeeklyReport_thenFail() {
    LocalDateTime monday = LocalDateTime.of(2024, 2, 12, 10, 0);

    // 아무 기록도 생성하지 않음

    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(weeklyUrl(monday.plusDays(2)))
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(400));
  }

  @Test
  @DisplayName("[E2E][weekly] 생활 기록 7일 + 배변 기록 0일 → 데이터 부족으로 리포트 생성 불가")
  void givenFullWeekActivityButNoToilet_whenGetWeeklyReport_thenFail() {
    LocalDateTime monday = LocalDateTime.of(2024, 2, 12, 10, 0);

    // 월~일 모두 생활 기록만 생성
    for (int i = 0; i < 7; i++) {
      createActivity(monday.plusDays(i));
    }

    // 배변 기록은 없음

    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(weeklyUrl(monday.plusDays(2)))
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(400));
  }

  // 6) 배변 기록이 하루만 있는 경우 → 실패
  @Test
  @DisplayName("[E2E][weekly] 배변 기록 1일만 존재 → 데이터 부족으로 리포트 생성 불가")
  void givenOnlyOneDayToiletRecord_whenGetWeeklyReport_thenFail() {
    LocalDateTime monday = LocalDateTime.of(2024, 2, 19, 10, 0);

    // 월요일에만 배변 기록 생성
    createToilet(
        monday.withHour(8), true, ToiletColor.DEFAULT, ToiletShape.BANANA, 10, 5, "1일만 기록");

    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(weeklyUrl(monday.plusDays(3)))
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(400));
  }

  // 7) 배변 기록이 2일 있는 경우 → 성공
  @Test
  @DisplayName("[E2E][weekly] 배변 기록 2일 존재 → 정상 리포트 반환")
  void givenTwoDaysToiletRecords_whenGetWeeklyReport_thenOk() {
    LocalDateTime monday = LocalDateTime.of(2024, 2, 26, 10, 0);

    // 월요일, 화요일에 배변 기록 생성
    createToilet(monday.withHour(8), true, ToiletColor.DEFAULT, ToiletShape.BANANA, 10, 5, "월요일");
    createToilet(
        monday.plusDays(1).withHour(9),
        true,
        ToiletColor.DARK_BROWN,
        ToiletShape.CORN,
        15,
        6,
        "화요일");

    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(weeklyUrl(monday.plusDays(4)))
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.defecationScore", notNullValue())
        .body("data.userAverage", notNullValue());
  }

  // 8) 생활 기록은 많지만 배변 기록은 1일만 → 실패
  @Test
  @DisplayName("[E2E][weekly] 생활 기록 7일 + 배변 기록 1일 → 데이터 부족으로 리포트 생성 불가")
  void givenManyActivityButOnlyOneToilet_whenGetWeeklyReport_thenFail() {
    LocalDateTime monday = LocalDateTime.of(2024, 3, 4, 10, 0);

    // 월~일 모두 생활 기록 생성 (7일)
    for (int i = 0; i < 7; i++) {
      createActivity(monday.plusDays(i));
    }

    // 배변 기록은 수요일에만 생성 (1일)
    createToilet(
        monday.plusDays(2).withHour(8),
        true,
        ToiletColor.DEFAULT,
        ToiletShape.BANANA,
        10,
        5,
        "수요일만");

    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(weeklyUrl(monday.plusDays(3)))
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(400));
  }

  // 9) 생활 기록은 없지만 배변 기록은 2일 → 성공
  @Test
  @DisplayName("[E2E][weekly] 생활 기록 없음 + 배변 기록 2일 → 정상 리포트 반환")
  void givenNoActivityButTwoToilets_whenGetWeeklyReport_thenOk() {
    LocalDateTime monday = LocalDateTime.of(2024, 3, 11, 10, 0);

    // 생활 기록은 없음
    // 배변 기록만 목요일, 금요일에 생성 (2일)
    createToilet(
        monday.plusDays(3).withHour(8),
        true,
        ToiletColor.DEFAULT,
        ToiletShape.BANANA,
        12,
        5,
        "목요일");
    createToilet(
        monday.plusDays(4).withHour(9), true, ToiletColor.GOLD, ToiletShape.CREAM, 8, 4, "금요일");

    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(weeklyUrl(monday.plusDays(5)))
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.defecationScore", notNullValue())
        .body("data.userAverage", notNullValue());
  }
}
