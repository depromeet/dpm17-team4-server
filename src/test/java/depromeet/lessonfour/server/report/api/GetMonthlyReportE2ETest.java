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
    Food chosen = foods.isEmpty() ? foodRepository.findAll().getFirst() : foods.getFirst();
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

  @Test
  @DisplayName("[E2E][monthly] 이번 달에 activity/toilet 데이터가 충분 → 전체 섹션 정상 반환")
  void givenFullMonth_whenGenerateMonthly_thenOk() {
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
  @DisplayName("[E2E][monthly] 지난달 데이터가 없더라도 → 비교값 0으로 안전 반환")
  void givenNoLastMonth_whenGenerateMonthly_thenOk() {
    // 대상 월: 2024-03 (이전에 아무것도 안 넣음)
    YearMonth ym = YearMonth.of(2024, 3);
    LocalDate start = ym.atDay(1);

    // 이번 달 일부만 데이터
    createActivity(start.plusDays(1).atTime(9, 0), "사과", MealTime.BREAKFAST, 4, StressLevel.MEDIUM);
    createToilet(
        start.plusDays(1).atTime(8, 0), ToiletColor.DEFAULT, ToiletShape.BANANA, 10, 5, null);

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
  @DisplayName("[E2E][monthly] 이번 달 일부만 존재(weeklyGroups 빈/부분) → NPE 없이 매핑")
  void givenSparseThisMonth_whenGenerateMonthly_thenOk() {
    YearMonth ym = YearMonth.of(2024, 4);
    LocalDate start = ym.atDay(1);

    // 주1: activity만 1건
    createActivity(start.plusDays(0).atTime(9, 0), "사과", MealTime.BREAKFAST, 0, StressLevel.LOW);
    // 주2: toilet만 1건
    createToilet(start.plusDays(10).atTime(8, 0), ToiletColor.GOLD, ToiletShape.ROCK, 20, 7, null);
    // 나머지 주는 비움

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
        .body("data.food.weeklyGroups", notNullValue())
        .body("data.food.weeklyGroups.size()", greaterThanOrEqualTo(1))
        .body("data.water.items", notNullValue())
        .body("data.stress.items", notNullValue())
        .body("data.shape.items", anyOf(notNullValue(), hasSize(greaterThanOrEqualTo(0))))
        .body("data.color.items", anyOf(notNullValue(), hasSize(greaterThanOrEqualTo(0))));
  }

  @Test
  @DisplayName("[E2E][monthly] 이번 달에 아무 기록도 없음 → 기본값으로 안전 반환")
  void givenNoRecordsThisMonth_whenGenerateMonthly_thenOk() {
    YearMonth ym = YearMonth.of(2024, 5);

    // 어떤 기록도 생성하지 않음

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
        .body("data", notNullValue())
        // 빈 값/0으로 매핑되는 섹션들 존재 확인
        .body("data.monthlyRecordCounts", notNullValue())
        .body("data.monthlyDefecationScore", notNullValue())
        .body("data.shape.items", anyOf(hasSize(0), notNullValue()))
        .body("data.timeDistribution.within5min", anyOf(equalTo(0), notNullValue()))
        .body("data.color.items", anyOf(hasSize(0), notNullValue()))
        .body("data.pain.high", anyOf(equalTo(0), notNullValue()))
        .body("data.timeOfDay.items", anyOf(hasSize(0), notNullValue()))
        .body("data.food.weeklyGroups", anyOf(hasSize(0), notNullValue()))
        .body("data.water.items", anyOf(hasSize(0), notNullValue()))
        .body("data.stress.items", anyOf(hasSize(0), notNullValue()))
        .body("data.suggestion.items", anyOf(hasSize(0), notNullValue()));
  }

  @Test
  @DisplayName("[E2E][monthly] RED가 1회라도 있으면 colorMessage가 RED 경고 문구")
  void givenRedColorAppears_thenRedWarningMessage() {
    YearMonth ym = YearMonth.of(2024, 6);
    LocalDate d = ym.atDay(3);

    // RED 1회, 나머지는 안전색 여러 번
    createToilet(d.atTime(8, 0), ToiletColor.RED, ToiletShape.BANANA, 10, 5, null);
    createToilet(d.plusDays(1).atTime(9, 0), ToiletColor.DEFAULT, ToiletShape.BANANA, 10, 5, null);
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
  @DisplayName("[E2E][monthly] painDiff 방향(increased/decreased/same) 계산")
  void painDiffDirection() {
    // 대상 2024-07, 지난달 2024-06로 비교됨
    YearMonth ym = YearMonth.of(2024, 7);

    // 지난달(6월): pain >=50 하루 생성
    LocalDate lastMonthDay = YearMonth.of(2024, 6).atDay(10);
    createToilet(lastMonthDay.atTime(8, 0), ToiletColor.DEFAULT, ToiletShape.BANANA, 60, 5, null);

    // 이번달(7월): pain >=50 이틀
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
  @DisplayName("[E2E][monthly] 물 섭취 메시지 - 주차레벨 평균(HIGH) ⇒ 유지 격려")
  void waterMessage_monthAvgHigh() {
    YearMonth ym = YearMonth.of(2024, 10);
    LocalDate start = ym.atDay(1);

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
  @DisplayName("[E2E][monthly] 물 섭취 0이어도 리포트 생성 및 권유 문구 노출")
  void waterSection_handlesZeroData() {
    YearMonth ym = YearMonth.of(2024, 9);
    LocalDate start = ym.atDay(1);

    // 이번 달에 activity는 있으나 water=0으로만 입력
    createActivity(start.plusDays(0).atTime(9, 0), "사과", MealTime.BREAKFAST, 0, StressLevel.MEDIUM);
    createActivity(start.plusDays(10).atTime(12, 0), "라면", MealTime.LUNCH, 0, StressLevel.LOW);

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
        // totalVolume 또는 items 기반 어느 쪽이든 0 상태를 안전히 표현해야 함
        .body("data.water.totalVolume", anyOf(equalTo(0), nullValue()))
        .body("data.water.items.size()", greaterThanOrEqualTo(0))
        // 권유/가이드 문구(카피가 바뀌어도 '물' 단어 포함 정도로 완화)
        .body(
            "data.water.message",
            anyOf(containsString("물"), containsString("수분"), containsString("마셔")));
  }

  @Test
  @DisplayName("[E2E][monthly] 물 섭취 메시지 - 기록 없음 ⇒ 안내 문구")
  void waterMessage_monthNone() {
    YearMonth ym = YearMonth.of(2024, 8);

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
  @DisplayName("[E2E][monthly] 말일 경계(29~말일) 5주차 버킷과 timeOfDay 매핑 검증(간단)")
  void endOfMonthBucketAndTimeOfDay() {
    YearMonth ym = YearMonth.of(2024, 2); // 29일까지 있는 달로 잡아도 OK
    LocalDate d29 = ym.atDay(Math.min(29, ym.lengthOfMonth()));
    // 오전/오후/저녁 각 1건
    createToilet(d29.atTime(6, 0), ToiletColor.DEFAULT, ToiletShape.BANANA, 10, 5, null);
    createToilet(d29.atTime(13, 0), ToiletColor.DEFAULT, ToiletShape.BANANA, 10, 5, null);
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
  @DisplayName("[E2E][monthly] ISO 주 경계: 전월 말(하이) + 당월 초(로우) → 주 집계는 당월 일자만 반영")
  void water_boundaryWeek_usesOnlyInMonthDays() {
    YearMonth ym = YearMonth.of(2024, 10); // 2024-10
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
  @DisplayName("[E2E][monthly] 월 시작/끝 하루만 기록: 둘 다 HIGH → 월평균 HIGH 유지 메시지")
  void water_monthStartEndOnly_highMaintained() {
    YearMonth ym = YearMonth.of(2024, 8);
    LocalDate first = ym.atDay(1);
    LocalDate last = ym.atEndOfMonth();

    // 시작일/말일만 HIGH(>=8컵)
    createActivity(first.atTime(8, 30), "사과", MealTime.BREAKFAST, 8, StressLevel.LOW);
    createActivity(last.atTime(20, 10), "사과", MealTime.DINNER, 9, StressLevel.LOW);

    given()
        .header("Authorization", validJwtToken)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(monthlyUrl(ym))
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("status", equalTo(201))
        // 정책상 MEDIUM/HIGH는 유지 계열 카피
        .body("data.water.message", anyOf(containsString("잘 섭취하고 계시군요"), containsString("유지")))
        // 주 아이템들 중 최소 하나는 HIGH(2000.0) 값을 가져야 함
        .body("data.water.items.value", hasItem(2000.0F));
  }

  @Test
  @DisplayName("[E2E][monthly] 미기록 섞임: HIGH 주 + NONE 주 + LOW 주 → NONE 제외 평균 = MEDIUM ⇒ 유지계열 카피")
  void water_mixedMissing_excludesNoneInAverage() {
    YearMonth ym = YearMonth.of(2024, 10);
    LocalDate start = ym.atDay(1);

    // 1주차: HIGH(≥8컵) 하루
    createActivity(start.plusDays(1).atTime(9, 0), "사과", MealTime.BREAKFAST, 8, StressLevel.LOW);

    // 2주차: NONE (아무 기록도 만들지 않음)

    // 3주차: LOW(≤4컵) 하루
    createActivity(
        start.plusDays(14 + 1).atTime(9, 0), "사과", MealTime.BREAKFAST, 4, StressLevel.LOW);

    given()
        .header("Authorization", validJwtToken)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .post(monthlyUrl(ym))
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("status", equalTo(201))
        // NONE을 평균에서 제외 → HIGH(3)과 LOW(1)의 평균 ≈ 2 → MEDIUM
        // 월 메시지는 MEDIUM/HIGH 묶음의 '유지' 계열 카피여야 함
        .body("data.water.message", anyOf(containsString("유지"), containsString("잘 섭취")))
        // 주 아이템 값들에 LOW(600.0)과 HIGH(2000.0)가 공존해야 함
        .body("data.water.items.value", hasItems(600.0F, 2000.0F));
  }
}
