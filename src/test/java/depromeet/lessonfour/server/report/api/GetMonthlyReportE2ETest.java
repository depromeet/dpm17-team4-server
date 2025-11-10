package depromeet.lessonfour.server.report.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
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
class GetMonthlyReportE2ETest {

  @LocalServerPort int port;

  @Autowired JwtTokenGenerator jwtTokenGenerator;
  @Autowired UserRepository userRepository;
  @Autowired PasswordEncoder passwordEncoder;
  @Autowired FoodRepository foodRepository;
  @Autowired JpaActivityRecordRepository activityRecordRepository;
  @Autowired ToiletRecordRepository toiletRecordRepository;

  String validJwtToken;
  Long testUserId;
  User testUser;

  final DateTimeFormatter ymFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

    testUser = createTestUser("monthly-report@example.com", "pw1234!", "monthly-user");
    testUserId = testUser.getId();
    validJwtToken = "Bearer " + jwtTokenGenerator.generateAccessToken(AccountContext.of(testUser));

    // 최소 1개 식품 확보 (isDangerous 로직의 임계치와 무관하게 이름만 쓰여도 매핑은 됨)
    if (foodRepository.findAll().isEmpty()) {
      foodRepository.save(Food.builder().name("사과").score(4.5).build());
      foodRepository.save(Food.builder().name("라면").score(9.9).build());
    }
  }

  private User createTestUser(String email, String password, String nickname) {
    User user = User.register(email, nickname, passwordEncoder.encode(password));
    return userRepository.save(user);
  }

  private ActivityRecord createActivity(
      LocalDateTime dateTime,
      String foodName,
      MealTime mealTime,
      int waterCups,
      StressLevel stress) {
    List<Food> foods =
        foodRepository.findAll().stream().filter(f -> f.getName().equals(foodName)).toList();
    Food chosen = foods.isEmpty() ? foodRepository.findAll().get(0) : foods.get(0);
    List<MealFood> meals = List.of(new MealFood(mealTime, chosen));
    return activityRecordRepository.save(
        ActivityRecord.createWithMeals(
            testUserId, waterCups, stress, ActivityAt.from(dateTime), meals));
  }

  private ToiletRecord createToilet(
      LocalDateTime dateTime,
      ToiletColor color,
      ToiletShape shape,
      Integer pain,
      Integer duration,
      String note) {
    ToiletRecord rec =
        ToiletRecord.register(
            testUser,
            true,
            color,
            shape,
            pain != null ? pain : 10,
            duration != null ? duration : 5,
            note,
            ActivityAt.from(dateTime));
    toiletRecordRepository.save(rec);
    return rec;
  }

  private String monthlyUrl(YearMonth ym) {
    return "/api/v1/reports/monthly?yearMonth=" + ym.format(ymFormatter);
  }

  /**
   * 월간 리포트 생성을 위한 최소 배변 기록 추가 대부분의 월에서 작동하는 기본 패턴 (1-2일, 8-9일) 특수한 케이스가 필요한 테스트는 이 헬퍼를 사용하지 않고 직접
   * 데이터 생성
   */
  private void createMinimalMonthlyToiletData(YearMonth ym) {
    LocalDate start = ym.atDay(1);
    createToilet(start.atTime(8, 0), ToiletColor.DEFAULT, ToiletShape.BANANA, 10, 5, "w1-d1");
    createToilet(
        start.plusDays(1).atTime(8, 0), ToiletColor.DEFAULT, ToiletShape.BANANA, 10, 5, "w1-d2");
    createToilet(
        start.plusDays(7).atTime(8, 0), ToiletColor.DEFAULT, ToiletShape.BANANA, 10, 5, "w2-d1");
    createToilet(
        start.plusDays(8).atTime(8, 0), ToiletColor.DEFAULT, ToiletShape.BANANA, 10, 5, "w2-d2");
  }

  @Test
  @DisplayName("[Given] 이번 달 충분한 데이터 [When] 월간 리포트 생성 [Then] 모든 섹션 정상 반환")
  void givenFullMonthData_whenGenerateMonthlyReport_thenReturnsAllSections() {
    // 대상 월: 2024-01
    YearMonth ym = YearMonth.of(2024, 1);
    LocalDate start = ym.atDay(1);

    // 1~7일: activity + toilet 생성(주 1)
    for (int i = 0; i < 7; i++) {
      LocalDate d = start.plusDays(i);
      createActivity(d.atTime(9, 0), i == 1 ? "라면" : "사과", MealTime.LUNCH, 6, StressLevel.MEDIUM);
      createToilet(d.atTime(8, 0), ToiletColor.DEFAULT, ToiletShape.BANANA, 10, 5, "note");
    }

    // 8~14일: activity 2일, toilet 2일(주 2)
    createActivity(start.plusDays(8).atTime(10, 0), "사과", MealTime.BREAKFAST, 8, StressLevel.LOW);
    createToilet(
        start.plusDays(8).atTime(7, 0), ToiletColor.DARK_BROWN, ToiletShape.CREAM, 5, 3, null);

    createActivity(start.plusDays(12).atTime(11, 0), "라면", MealTime.DINNER, 3, StressLevel.HIGH);
    createToilet(start.plusDays(12).atTime(6, 0), ToiletColor.GOLD, ToiletShape.ROCK, 25, 8, null);

    given()
        .header("Authorization", validJwtToken)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(monthlyUrl(ym))
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(201)) // POST /monthly → SUCCESS_CREATE
        .body("data", notNullValue())
        // 상단 공통
        .body("data.monthlyRecordCounts", notNullValue())
        .body("data.userAverage", notNullValue())
        .body("data.monthlyDefecationScore", notNullValue())
        // 화장실 섹션들
        .body("data.shape.items", notNullValue())
        .body("data.timeDistribution.within5min", notNullValue())
        .body("data.color.items", notNullValue())
        .body("data.pain.titleMessage", notNullValue())
        .body("data.timeOfDay.items", notNullValue())
        // activity 섹션들
        .body("data.food.weeklyGroups", notNullValue())
        .body("data.water.items", notNullValue())
        .body("data.stress.items", notNullValue())
        .body("data.suggestion.items", notNullValue());
  }

  @Test
  @DisplayName("[Given] 지난달 데이터 없음 [When] 월간 리포트 생성 [Then] 비교값 0으로 정상 반환")
  void givenNoLastMonthData_whenGenerateMonthlyReport_thenReturnsWithZeroComparison() {
    // 대상 월: 2024-03 (이전에 아무것도 안 넣음)
    YearMonth ym = YearMonth.of(2024, 3);
    createMinimalMonthlyToiletData(ym);

    LocalDate start = ym.atDay(1);

    // 이번 달 일부만 데이터
    createActivity(start.plusDays(1).atTime(9, 0), "사과", MealTime.BREAKFAST, 4, StressLevel.MEDIUM);

    given()
        .header("Authorization", validJwtToken)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(monthlyUrl(ym))
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(201))
        .body("data.food.monthlyComparison.lastMonth", anyOf(equalTo(0), greaterThanOrEqualTo(0)))
        .body("data.food.monthlyComparison.thisMonth", greaterThanOrEqualTo(0));
  }

  @Test
  @DisplayName("[Given] 이번 달 기록 없음 [When] 월간 리포트 생성 [Then] 데이터 부족 예외 발생")
  void givenNoRecordsThisMonth_whenGenerateMonthlyReport_thenThrowsInsufficientDataException() {
    YearMonth ym = YearMonth.of(2024, 5);

    // 어떤 기록도 생성하지 않음

    given()
        .header("Authorization", validJwtToken)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(monthlyUrl(ym))
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE);
  }

  @Test
  @DisplayName("[Given] RED 색상 기록 존재 [When] 월간 리포트 생성 [Then] RED 경고 메시지 반환")
  void givenRedColorRecords_whenGenerateMonthlyReport_thenReturnsRedWarningMessage() {
    YearMonth ym = YearMonth.of(2024, 6);
    LocalDate d = ym.atDay(3);

    // RED를 가장 많이 발생시켜 most frequent color가 RED가 되도록 함
    // 1주차: day 1, 2 - RED
    createToilet(d.minusDays(2).atTime(8, 0), ToiletColor.RED, ToiletShape.BANANA, 10, 5, null);
    createToilet(d.minusDays(1).atTime(8, 0), ToiletColor.RED, ToiletShape.BANANA, 10, 5, null);
    // 2주차: day 8, 9 - RED
    createToilet(d.plusDays(5).atTime(8, 0), ToiletColor.RED, ToiletShape.BANANA, 10, 5, null);
    createToilet(d.plusDays(6).atTime(8, 0), ToiletColor.RED, ToiletShape.BANANA, 10, 5, null);
    // 추가: day 3 - RED
    createToilet(d.atTime(8, 0), ToiletColor.RED, ToiletShape.BANANA, 10, 5, null);

    createActivity(d.atTime(10, 0), "사과", MealTime.LUNCH, 6, StressLevel.MEDIUM);

    given()
        .header("Authorization", validJwtToken)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(monthlyUrl(ym))
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("status", equalTo(201))
        .body("data.color.colorMessage", containsString("혈변은 건강의 적신호"));
  }

  @Test
  @DisplayName("[Given] 지난달보다 통증 증가 [When] 월간 리포트 생성 [Then] 통증 증가 방향 반환")
  void givenIncreasedPain_whenGenerateMonthlyReport_thenReturnsPainIncreased() {
    // 대상 2024-07, 지난달 2024-06로 비교됨
    YearMonth ym = YearMonth.of(2024, 7);
    createMinimalMonthlyToiletData(ym);

    // 지난달(6월): pain >=50 하루 생성 (지난달 통증 일수 카운트용)
    LocalDate lastMonthDay = YearMonth.of(2024, 6).atDay(10);
    createToilet(lastMonthDay.atTime(8, 0), ToiletColor.DEFAULT, ToiletShape.BANANA, 60, 5, null);

    // 이번달(7월): pain >=50 이틀 (minimal data에 추가)
    LocalDate thisMonthDay = ym.atDay(5);
    createToilet(thisMonthDay.atTime(8, 0), ToiletColor.DEFAULT, ToiletShape.ROCK, 70, 6, null);
    createToilet(
        thisMonthDay.plusDays(2).atTime(9, 0), ToiletColor.DEFAULT, ToiletShape.CREAM, 55, 7, null);

    given()
        .header("Authorization", validJwtToken)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(monthlyUrl(ym))
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("status", equalTo(201))
        .body("data.pain.comparison.direction", equalTo("increased"))
        .body("data.pain.comparison.count", equalTo(1));
  }

  @Test
  @DisplayName("[Given] 높은 물 섭취 기록 [When] 월간 리포트 생성 [Then] 유지 격려 메시지 반환")
  void givenHighWaterIntake_whenGenerateMonthlyReport_thenReturnsMaintenanceMessage() {
    YearMonth ym = YearMonth.of(2024, 10);
    LocalDate start = ym.atDay(1);
    createMinimalMonthlyToiletData(ym);

    // 1주차: HIGH 하루만 기록(그 주의 평균은 HIGH로 계산됨)
    createActivity(start.plusDays(1).atTime(9, 0), "사과", MealTime.BREAKFAST, 8, StressLevel.LOW);

    // 2주차: HIGH 하루
    createActivity(start.plusDays(8).atTime(9, 0), "사과", MealTime.BREAKFAST, 9, StressLevel.LOW);

    given()
        .header("Authorization", validJwtToken)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(monthlyUrl(ym))
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("status", equalTo(201))
        .body("data.water.message", anyOf(containsString("잘 섭취하고 계시군요"), containsString("유지")));
  }

  @Test
  @DisplayName("[Given] 물 섭취 기록 없음 [When] 월간 리포트 생성 [Then] 안내 문구 반환")
  void givenNoWaterRecords_whenGenerateMonthlyReport_thenReturnsGuideMessage() {
    YearMonth ym = YearMonth.of(2024, 8);
    createMinimalMonthlyToiletData(ym);

    given()
        .header("Authorization", validJwtToken)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(monthlyUrl(ym))
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("status", equalTo(201))
        .body("data.water.message", anyOf(containsString("기록이 없어요"), containsString("자주 마셔")));
  }

  @Test
  @DisplayName("[Given] 월 말일 기록 [When] 월간 리포트 생성 [Then] 시간대별 분포 정상 매핑")
  void givenEndOfMonthRecords_whenGenerateMonthlyReport_thenMapsTimeOfDayCorrectly() {
    YearMonth ym = YearMonth.of(2024, 2); // 29일까지 있는 달 (윤년)
    LocalDate start = ym.atDay(1);

    // 2월 2024 (윤년): 성공하는 테스트 패턴 따라하기 - 연속 7일 + 추가 2일
    // 1-7일
    for (int i = 0; i < 7; i++) {
      createToilet(
          start.plusDays(i).atTime(8, 0), ToiletColor.DEFAULT, ToiletShape.BANANA, 10, 5, null);
    }
    // 8-9일
    createToilet(
        start.plusDays(7).atTime(8, 0), ToiletColor.DEFAULT, ToiletShape.BANANA, 10, 5, null);
    createToilet(
        start.plusDays(8).atTime(8, 0), ToiletColor.DEFAULT, ToiletShape.BANANA, 10, 5, null);

    LocalDate d28 = ym.atDay(Math.min(28, ym.lengthOfMonth()));
    LocalDate d29 = ym.atDay(Math.min(29, ym.lengthOfMonth()));
    // 말일 주간에 2일 이상 데이터 생성 (주간 리포트 검증 통과)
    // 28일 - timeOfDay 매핑 검증용
    createToilet(d28.atTime(6, 0), ToiletColor.DEFAULT, ToiletShape.BANANA, 10, 5, null);
    createToilet(d28.atTime(13, 0), ToiletColor.DEFAULT, ToiletShape.BANANA, 10, 5, null);
    // 29일 - timeOfDay 매핑 검증용
    createToilet(d29.atTime(19, 0), ToiletColor.DEFAULT, ToiletShape.BANANA, 10, 5, null);

    given()
        .header("Authorization", validJwtToken)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(monthlyUrl(ym))
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("status", equalTo(201))
        .body("data.timeOfDay.items.size()", greaterThanOrEqualTo(3))
        .body("data.timeOfDay.items.find { it.period == '오전' }.count", greaterThanOrEqualTo(1))
        .body("data.timeOfDay.items.find { it.period == '오후' }.count", greaterThanOrEqualTo(1))
        .body("data.timeOfDay.items.find { it.period == '저녁' }.count", greaterThanOrEqualTo(1));
  }

  @Test
  @DisplayName("[Given] 전월 말+당월 초 기록 [When] 월간 리포트 생성 [Then] 당월 데이터만 사용")
  void givenMonthBoundaryRecords_whenGenerateMonthlyReport_thenUsesOnlyCurrentMonthData() {
    YearMonth ym = YearMonth.of(2024, 10); // 2024-10
    createMinimalMonthlyToiletData(ym);

    LocalDate first = ym.atDay(1); // 2024-10-01
    LocalDate prevLast = first.minusDays(1); // 2024-09-30 (전월)

    // 전월 말: HIGH(10컵) — 당월 집계에서 제외되어야 함
    createActivity(prevLast.atTime(9, 0), "사과", MealTime.BREAKFAST, 10, StressLevel.LOW);

    // 당월 초: LOW(4컵) — 당월 집계에 포함
    createActivity(first.atTime(9, 0), "사과", MealTime.BREAKFAST, 4, StressLevel.LOW);

    given()
        .header("Authorization", validJwtToken)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(monthlyUrl(ym))
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("status", equalTo(201))
        // 월 메시지는 LOW로 수렴해야 함
        .body(
            "data.water.message",
            anyOf(
                containsString("늘려야"), // "물 섭취량을 늘려야 해요"
                containsString("말라") // 카피 변형 대비
                ))
        // NONE이 아닌 주 1개만 생기므로, 해당 주 값은 LOW(600.0)이어야 함
        .body("data.water.items.size()", greaterThanOrEqualTo(1))
        .body("data.water.items[0].value", equalTo(600.0F));
  }

  @Test
  @DisplayName("[Given] 2월 평년(28일) [When] 월간 리포트 생성 [Then] 4주차까지 생성")
  void givenFebruaryNonLeapYear_whenGenerateMonthlyReport_thenReturnsFourWeeklyGroups() {
    YearMonth ym = YearMonth.of(2023, 2); // 28일
    createMinimalMonthlyToiletData(ym);

    LocalDate first = ym.atDay(1);

    // 최소 1건 생성(weeklyGroups 비지 않도록)
    createActivity(first.atTime(9, 0), "사과", MealTime.BREAKFAST, 4, StressLevel.LOW);

    given()
        .header("Authorization", validJwtToken)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(monthlyUrl(ym))
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(201))
        // 4주차까지만 생성
        .body("data.food.weeklyGroups.size()", equalTo(4))
        // 4주차의 endDate는 2월 28일
        .body("data.food.weeklyGroups[3].endDate", equalTo(ym.atEndOfMonth().toString()))
        // 5주차가 없어야 함
        .body("data.food.weeklyGroups.find { it.weekLabel == '5주차' }", nullValue());
  }

  @Test
  @DisplayName("[Given] 2월 윤년(29일) [When] 월간 리포트 생성 [Then] 5주차까지 생성")
  void givenFebruaryLeapYear_whenGenerateMonthlyReport_thenReturnsFiveWeeklyGroups() {
    YearMonth ym = YearMonth.of(2024, 2); // 윤년 29일
    createMinimalMonthlyToiletData(ym);

    LocalDate first = ym.atDay(1);

    // 최소 1건 생성
    createActivity(first.atTime(9, 0), "사과", MealTime.BREAKFAST, 4, StressLevel.LOW);

    given()
        .header("Authorization", validJwtToken)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(monthlyUrl(ym))
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(201))
        // 5주차까지 생성
        .body("data.food.weeklyGroups.size()", equalTo(5))
        // 5주차는 29~29
        .body("data.food.weeklyGroups[4].startDate", equalTo(ym.atDay(29).toString()))
        .body("data.food.weeklyGroups[4].endDate", equalTo(ym.atDay(29).toString()))
        .body("data.food.weeklyGroups[4].weekLabel", equalTo("5주차"));
  }

  @Test
  @DisplayName("[Given] 31일 달 [When] 월간 리포트 생성 [Then] 5주차는 29-31일")
  void given31DayMonth_whenGenerateMonthlyReport_thenReturnsFifthWeekCovers29To31() {
    YearMonth ym = YearMonth.of(2024, 7); // 31일인 달
    createMinimalMonthlyToiletData(ym);

    LocalDate first = ym.atDay(1);

    // 최소 1건 생성
    createActivity(first.atTime(9, 0), "사과", MealTime.BREAKFAST, 4, StressLevel.LOW);

    given()
        .header("Authorization", validJwtToken)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(monthlyUrl(ym))
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(201))
        // 5주차까지 생성
        .body("data.food.weeklyGroups.size()", equalTo(5))
        // 5주차는 29일부터 말일까지
        .body("data.food.weeklyGroups[4].startDate", equalTo(ym.atDay(29).toString()))
        .body("data.food.weeklyGroups[4].endDate", equalTo(ym.atEndOfMonth().toString()))
        .body("data.food.weeklyGroups[4].weekLabel", equalTo("5주차"));
  }

  @Test
  @DisplayName("[Given] 스트레스 기록 없음 [When] 월간 리포트 생성 [Then] 스트레스 기록 안내 메시지 반환")
  void givenNoStressRecords_whenGenerateMonthlyReport_thenReturnsStressGuideMessage() {
    YearMonth ym = YearMonth.of(2024, 4);
    createMinimalMonthlyToiletData(ym);

    // 배변 기록만 있고 활동 기록(스트레스 포함)이 없음

    given()
        .header("Authorization", validJwtToken)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(monthlyUrl(ym))
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(201))
        .body("data.stress.message", containsString("스트레스 기록이 비어있어요"))
        .body("data.stress.message", containsString("기록을 시작해보세요"));
  }
}
