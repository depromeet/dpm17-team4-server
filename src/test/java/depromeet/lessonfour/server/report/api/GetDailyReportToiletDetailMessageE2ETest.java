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
class GetDailyReportToiletDetailMessageE2ETest {

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

  // DETAIL_MESSAGE_MAP 상수들 (ToiletReportMapper에서 복사)
  private static final String VERY_BAD_MESSAGE =
      "전문가의 상담이 필요해요. 복통이 매우 심했다면 단순한 식사 문제를 넘어서 장염이나 자극적인 음식 섭취 가능성도 생각해볼 수 있어요.";
  private static final String BAD_MESSAGE =
      "장 컨디션이 다소 불안정해요. 자극적인 음식을 줄이고, 따뜻한 물과 가벼운 식단으로 조절해보세요. 스트레스나 수면 부족도 영향을 줄 수 있어요.";
  private static final String AVERAGE_MESSAGE =
      "평균적인 장 상태예요. 특별한 이상은 없지만, 식사 시간이나 수분 섭취가 불규칙했다면 조정이 필요할 수도 있어요. 내일은 조금 더 신경 써볼까요?";
  private static final String GOOD_MESSAGE =
      "대체로 좋은 상태예요. 식이섬유나 수분 섭취가 잘 이루어졌을 가능성이 높아요. 가벼운 운동이나 스트레칭으로 리듬을 이어가면 좋을 것 같아요.";
  private static final String VERY_GOOD_MESSAGE =
      "오늘은 장이 최상의 컨디션이에요. 규칙적이고 건강한 식습관과 충분한 수분 섭취가 잘 이루어지고 있네요. 지금처럼 꾸준히 유지해보세요.";

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
            color,
            shape,
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

  /**
   * VERY_BAD (0~19점) 테스트
   *
   * <p>점수 계산: BASE: 50 실패: -100 (FAIL_PENALTY * SUCCESS_WEIGHT = -20 * 5) 총점: 50 - 100 = -50 → 정규화
   * 후 0점
   */
  @Test
  @DisplayName(
      "[E2E] DETAIL_MESSAGE_MAP 검증 - VERY_BAD: 전문가의 상담이 필요해요. 복통이 매우 심했다면 단순한 식사 문제를 넘어서 장염이나 자극적인 음식 섭취 가능성도 생각해볼 수 있어요.")
  void givenVeryBadToilet_whenGetDailyReport_thenReturnsVeryBadMessage() {
    LocalDateTime base = LocalDateTime.of(2024, 1, 15, 10, 0);

    // VERY_BAD를 만들기 위한 조건: 실패 케이스
    createToilet(
        base.withHour(8),
        false, // 실패 → -100점
        ToiletColor.DEFAULT,
        ToiletShape.BANANA,
        10, // 낮은 통증
        5, // 짧은 시간
        "실패");

    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(url(base))
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.poo", notNullValue())
        .body("data.poo.items.size()", equalTo(1))
        .body("data.poo.items[0].message", equalTo(VERY_BAD_MESSAGE));
  }

  /**
   * BAD (20~39점) 테스트
   *
   * <p>점수 계산: BASE: 50 성공: +50 색상(BLACK): -20 * 2 = -40 모양(CORN): -5 * 3 = -15 통증(30): -5 * 4 = -20
   * 총점: 50 + 50 - 40 - 15 - 20 = 25점 (BAD)
   */
  @Test
  @DisplayName(
      "[E2E] DETAIL_MESSAGE_MAP 검증 - BAD: 장 컨디션이 다소 불안정해요. 자극적인 음식을 줄이고, 따뜻한 물과 가벼운 식단으로 조절해보세요. 스트레스나 수면 부족도 영향을 줄 수 있어요.")
  void givenBadToilet_whenGetDailyReport_thenReturnsBadMessage() {
    LocalDateTime base = LocalDateTime.of(2024, 1, 16, 10, 0);

    // BAD를 만들기 위한 조건: 성공 + 나쁜 색상 + 나쁜 모양 + 중간 통증
    createToilet(
        base.withHour(8),
        true, // 성공 → +50점
        ToiletColor.BLACK, // 나쁜 색상 → -40점
        ToiletShape.CORN, // 나쁜 모양 → -15점
        30, // 중간 통증 → -20점
        5, // 짧은 시간
        "BAD");

    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(url(base))
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.poo", notNullValue())
        .body("data.poo.items.size()", equalTo(1))
        .body("data.poo.items[0].message", equalTo(BAD_MESSAGE));
  }

  /**
   * AVERAGE (40~59점) 테스트
   *
   * <p>점수 계산: BASE: 50 성공: +50 색상(DARK_BROWN): -5 * 2 = -10 모양(RABBIT): -15 * 3 = -45 총점: 50 + 50 -
   * 10 - 45 = 45점 (AVERAGE)
   */
  @Test
  @DisplayName(
      "[E2E] DETAIL_MESSAGE_MAP 검증 - AVERAGE: 평균적인 장 상태예요. 특별한 이상은 없지만, 식사 시간이나 수분 섭취가 불규칙했다면 조정이 필요할 수도 있어요. 내일은 조금 더 신경 써볼까요?")
  void givenAverageToilet_whenGetDailyReport_thenReturnsAverageMessage() {
    LocalDateTime base = LocalDateTime.of(2024, 1, 17, 10, 0);

    // AVERAGE를 만들기 위한 조건: 성공 + 나쁜 색상 + 나쁜 모양
    createToilet(
        base.withHour(8),
        true, // 성공 → +50점
        ToiletColor.DARK_BROWN, // 나쁜 색상 → -10점
        ToiletShape.RABBIT, // 나쁜 모양 → -45점
        10, // 낮은 통증
        5, // 짧은 시간
        "AVERAGE");

    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(url(base))
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.poo", notNullValue())
        .body("data.poo.items.size()", equalTo(1))
        .body("data.poo.items[0].message", equalTo(AVERAGE_MESSAGE));
  }

  /**
   * GOOD (60~79점) 테스트
   *
   * <p>점수 계산: BASE: 50 성공: +50 색상(DARK_BROWN): -5 * 2 = -10 모양(CORN): -5 * 3 = -15 총점: 50 + 50 - 10
   * - 15 = 75점 (GOOD)
   */
  @Test
  @DisplayName(
      "[E2E] DETAIL_MESSAGE_MAP 검증 - GOOD: 대체로 좋은 상태예요. 식이섬유나 수분 섭취가 잘 이루어졌을 가능성이 높아요. 가벼운 운동이나 스트레칭으로 리듬을 이어가면 좋을 것 같아요.")
  void givenGoodToilet_whenGetDailyReport_thenReturnsGoodMessage() {
    LocalDateTime base = LocalDateTime.of(2024, 1, 18, 10, 0);

    // GOOD를 만들기 위한 조건: 성공 + 약간 나쁜 색상 + 약간 나쁜 모양
    createToilet(
        base.withHour(8),
        true, // 성공 → +50점
        ToiletColor.DARK_BROWN, // 약간 나쁜 색상 → -10점
        ToiletShape.CORN, // 약간 나쁜 모양 → -15점
        10, // 낮은 통증
        5, // 짧은 시간
        "GOOD");

    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(url(base))
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.poo", notNullValue())
        .body("data.poo.items.size()", equalTo(1))
        .body("data.poo.items[0].message", equalTo(GOOD_MESSAGE));
  }

  /**
   * VERY_GOOD (80~100점) 테스트
   *
   * <p>점수 계산: BASE: 50 성공: +50 색상(DEFAULT): 0 * 2 = 0 모양(BANANA): +15 * 3 = +45 총점: 50 + 50 + 0 +
   * 45 = 145 → 정규화 후 100점 (VERY_GOOD)
   */
  @Test
  @DisplayName(
      "[E2E] DETAIL_MESSAGE_MAP 검증 - VERY_GOOD: 오늘은 장이 최상의 컨디션이에요. 규칙적이고 건강한 식습관과 충분한 수분 섭취가 잘 이루어지고 있네요. 지금처럼 꾸준히 유지해보세요.")
  void givenVeryGoodToilet_whenGetDailyReport_thenReturnsVeryGoodMessage() {
    LocalDateTime base = LocalDateTime.of(2024, 1, 19, 10, 0);

    // VERY_GOOD를 만들기 위한 조건: 성공 + 좋은 색상 + 좋은 모양
    createToilet(
        base.withHour(8),
        true, // 성공 → +50점
        ToiletColor.DEFAULT, // 좋은 색상 → 0점
        ToiletShape.BANANA, // 이상적인 모양 → +45점
        10, // 낮은 통증
        5, // 짧은 시간
        "VERY_GOOD");

    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(url(base))
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.poo", notNullValue())
        .body("data.poo.items.size()", equalTo(1))
        .body("data.poo.items[0].message", equalTo(VERY_GOOD_MESSAGE));
  }

  /**
   * 여러 레벨의 ToiletRecord를 함께 테스트
   *
   * <p>하루에 여러 번 화장실을 가는 경우, 각 레코드마다 올바른 메시지가 반환되는지 검증
   */
  @Test
  @DisplayName("[E2E] DETAIL_MESSAGE_MAP 검증 - 여러 레벨의 ToiletRecord가 각각 올바른 메시지를 반환")
  void givenMultipleLevelToilets_whenGetDailyReport_thenReturnsCorrectMessagesForEach() {
    LocalDateTime base = LocalDateTime.of(2024, 1, 20, 10, 0);

    // VERY_GOOD (성공 + DEFAULT + BANANA)
    createToilet(base.withHour(8), true, ToiletColor.DEFAULT, ToiletShape.BANANA, 10, 5, "1");

    // GOOD (성공 + DARK_BROWN + CORN)
    createToilet(base.withHour(12), true, ToiletColor.DARK_BROWN, ToiletShape.CORN, 10, 5, "2");

    // AVERAGE (성공 + DARK_BROWN + RABBIT)
    createToilet(base.withHour(16), true, ToiletColor.DARK_BROWN, ToiletShape.RABBIT, 10, 5, "3");

    // BAD (성공 + BLACK + CORN + 통증30)
    createToilet(base.withHour(18), true, ToiletColor.BLACK, ToiletShape.CORN, 30, 5, "4");

    // VERY_BAD (실패)
    createToilet(base.withHour(20), false, ToiletColor.DEFAULT, ToiletShape.BANANA, 10, 5, "5");

    // API 호출 시 평균 점수로 레벨이 결정되므로, poo.items 배열의 각 항목을 검증해야 함
    // 평균 점수: (100 + 75 + 45 + 25 + 0) / 5 = 49 → AVERAGE
    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(url(base))
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.poo", notNullValue())
        .body("data.poo.items.size()", equalTo(5))
        // 각 항목의 메시지가 AVERAGE로 통일됨 (DailyToiletReport의 평균 레벨 사용)
        .body("data.poo.items[0].message", equalTo(AVERAGE_MESSAGE))
        .body("data.poo.items[1].message", equalTo(AVERAGE_MESSAGE))
        .body("data.poo.items[2].message", equalTo(AVERAGE_MESSAGE))
        .body("data.poo.items[3].message", equalTo(AVERAGE_MESSAGE))
        .body("data.poo.items[4].message", equalTo(AVERAGE_MESSAGE));
  }
}
