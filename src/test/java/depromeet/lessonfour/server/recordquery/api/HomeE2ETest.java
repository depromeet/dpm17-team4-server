package depromeet.lessonfour.server.recordquery.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

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
class HomeE2ETest {

  @LocalServerPort int port;

  @Autowired JwtTokenGenerator jwtTokenGenerator;
  @Autowired UserRepository userRepository;
  @Autowired PasswordEncoder passwordEncoder;
  @Autowired FoodRepository foodRepository;

  private String validJwtToken;
  private User testUser;
  private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private final DateTimeFormatter dateTimeFormatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

    testUser = createTestUser("home@test.com", "pw1234", "home-user");
    validJwtToken = "Bearer " + jwtTokenGenerator.generateAccessToken(AccountContext.of(testUser));

    // 테스트용 Food 한 개
    if (foodRepository.count() == 0) {
      Food f = Food.builder().name("사과").score(4.5).build();
      foodRepository.save(f);
    }
  }

  private User createTestUser(String email, String rawPw, String nickname) {
    User u = User.register(email, nickname, passwordEncoder.encode(rawPw));
    return userRepository.save(u);
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
          "note": "note"
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

  @Test
  @DisplayName("[home] 점수 75(GOOD) + 화장실 2건 + 활동존재 → hero=GOOD 이미지/컬러")
  void home_ok_good() {
    LocalDate date = LocalDate.of(2024, 10, 5);
    // score=75 → GOOD(60–79)
    // 2개 기록의 평균: (85+65)/2 = 75
    // 기록1: 50+50+0-15(CORN)+0+0 = 85
    // 기록2: 50+50+0-15(CORN)+0-20(통증25) = 65
    createToilet(LocalDateTime.of(2024, 10, 5, 8, 0), "DEFAULT", "CORN", 10, 5);
    createToilet(LocalDateTime.of(2024, 10, 5, 14, 0), "DEFAULT", "CORN", 25, 5);

    // activity exists
    createActivity(LocalDateTime.of(2024, 10, 5, 10, 0));

    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get("/api/v1/home/" + date.format(dateFormatter))
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.toiletRecordCount", equalTo(2))
        .body("data.hasActivityRecord", equalTo(true))
        // GOOD 매핑 확인
        .body("data.heroImage", containsString("toilet/good.png"))
        .body("data.heroBackgroundColors", hasItems("#134DB1", "#588DFF"));
  }

  @Test
  @DisplayName("[home] 점수 85(VERY_GOOD) + 화장실 1건 + 활동없음 → hero=VERY_GOOD 이미지/컬러")
  void home_ok_veryGood() {
    LocalDate date = LocalDate.of(2024, 10, 6);
    createToilet(LocalDateTime.of(2024, 10, 6, 8, 0), "GOLD", "BANANA", 10, 5);

    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get("/api/v1/home/" + date.format(dateFormatter))
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.toiletRecordCount", equalTo(1))
        .body("data.hasActivityRecord", equalTo(false))
        .body("data.heroImage", containsString("toilet/very_good.png"))
        .body("data.heroBackgroundColors", hasItems("#0C7C30", "#7DD357"));
  }

  @Test
  @DisplayName("[home] 점수 15(VERY_BAD) → hero=VERY_BAD 이미지/컬러")
  void home_ok_veryBad() {
    LocalDate date = LocalDate.of(2024, 10, 7);
    // score=15 → VERY_BAD(0–19)
    // 1개 기록: 50+50-40(RED)-45(RABBIT)+0+0 = 15
    createToilet(LocalDateTime.of(2024, 10, 7, 8, 0), "RED", "RABBIT", 100, 15);

    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get("/api/v1/home/" + date.format(dateFormatter))
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.heroImage", containsString("toilet/very_bad.png"))
        .body("data.heroBackgroundColors", hasItems("#A4141E", "#FF535F"));
  }

  @Test
  @DisplayName("[home] 경계값 매핑 확인: 60(GOOD), 40(AVERAGE), 20(BAD)")
  void home_ok_boundaries() {
    // 60 → GOOD
    // 기록: 60 + 50 - 10(DARK_BROWN) - 30(PORRIDGE) + 0 + 0 = 60
    LocalDate d1 = LocalDate.of(2024, 10, 8);
    createToilet(LocalDateTime.of(2024, 10, 8, 8, 0), "DARK_BROWN", "BANANA", 10, 5);
    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get("/api/v1/home/" + d1.format(dateFormatter))
        .then()
        .statusCode(200)
        .body("data.heroImage", containsString("toilet/good.png"))
        .body("data.heroBackgroundColors", hasItems("#134DB1", "#588DFF"));

    // 40 → AVERAGE
    // 기록: 50+50-10(DARK_BROWN)-30(PORRIDGE)-20(통증25)+0 = 40
    LocalDate d2 = LocalDate.of(2024, 10, 9);
    createToilet(LocalDateTime.of(2024, 10, 9, 8, 0), "DARK_BROWN", "PORRIDGE", 25, 5);
    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get("/api/v1/home/" + d2.format(dateFormatter))
        .then()
        .statusCode(200)
        .body("data.heroImage", containsString("toilet/normal.png"))
        .body("data.heroBackgroundColors", hasItems("#2B42B4", "#8F58FF"));

    // 20 → BAD
    // 기록: 50+50-40(RED)-30(PORRIDGE)+0-10(시간15분) = 20
    LocalDate d3 = LocalDate.of(2024, 10, 10);
    createToilet(LocalDateTime.of(2024, 10, 10, 8, 0), "RED", "PORRIDGE", 10, 15);
    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get("/api/v1/home/" + d3.format(dateFormatter))
        .then()
        .statusCode(200)
        .body("data.heroImage", containsString("toilet/bad.png"))
        .body("data.heroBackgroundColors", hasItems("#DD5612", "#F6A85F"));
  }

  @Test
  @DisplayName("[home] 점수/활동/배변 모두 없음 → 카운트0/false, hero=AVERAGE 기본 에셋")
  void home_noData_defaultsToAverage() {
    // Given: 아무 데이터도 넣지 않음
    LocalDate date = LocalDate.of(2024, 10, 12);

    // When & Then
    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get("/api/v1/home/" + date.format(dateFormatter))
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.toiletRecordCount", equalTo(0))
        .body("data.hasActivityRecord", equalTo(false))
        // 점수 미기록 → ReportClient가 0으로 보고 → AVERAGE 매핑
        .body("data.heroImage", containsString("toilet/normal.png"))
        .body("data.heroBackgroundColors", hasItems("#2B42B4", "#8F58FF"));
  }

  @Test
  @DisplayName("[home] JWT 없이 접근 시 401")
  void home_unauthorized_noToken() {
    LocalDate date = LocalDate.of(2024, 10, 5);
    given()
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get("/api/v1/home/" + date.format(dateFormatter))
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  @DisplayName("[home] 잘못된 날짜 형식 400")
  void home_badRequest_invalidDate() {
    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get("/api/v1/home/invalid-date")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  @DisplayName("[home] 다른 사용자 데이터는 카운트/활동/점수에 반영되지 않는다")
  void home_otherUser_notCounted() {
    User other = createTestUser("other@ex.com", "pw", "other");
    String otherJwtToken =
        "Bearer " + jwtTokenGenerator.generateAccessToken(AccountContext.of(other));
    LocalDate date = LocalDate.of(2024, 10, 11);

    // 다른 사용자 생활 기록 생성
    List<Food> foods = foodRepository.findAll();
    Long foodId = foods.getFirst().getId();
    String activityRequest =
        String.format(
            """
        {
          "foods": [{"id": %d, "mealTime": "BREAKFAST"}],
          "water": 5,
          "stress": "MEDIUM",
          "occurredAt": "%s"
        }
        """,
            foodId, LocalDateTime.of(2024, 10, 11, 9, 0).format(dateTimeFormatter));

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", otherJwtToken)
        .body(activityRequest)
        .when()
        .post("/api/v1/activity-records")
        .then()
        .statusCode(HttpStatus.CREATED.value());

    // 다른 사용자 배변 기록 생성
    String toiletRequest =
        String.format(
            """
        {
          "occurredAt": "%s",
          "isSuccessful": true,
          "color": "DEFAULT",
          "shape": "BANANA",
          "pain": 20,
          "duration": 5,
          "note": "other"
        }
        """,
            LocalDateTime.of(2024, 10, 11, 8, 0).format(dateTimeFormatter));

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", otherJwtToken)
        .body(toiletRequest)
        .when()
        .post("/api/v1/poo-records")
        .then()
        .statusCode(HttpStatus.OK.value());

    // 다른 사용자의 배변 기록은 자동으로 점수 계산됨
    // 기록: 50+50+0+45(BANANA)+0+0 = 145 (capped at 100, VERY_GOOD)

    // 현재 사용자로 조회 → 0/false + hero는 기본(점수 없으면 AVERAGE로 매핑됨)
    given()
        .header("Authorization", validJwtToken)
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get("/api/v1/home/" + date.format(dateFormatter))
        .then()
        .statusCode(200)
        .body("status", equalTo(200))
        .body("data.toiletRecordCount", equalTo(0))
        .body("data.hasActivityRecord", equalTo(false))
        // 점수 미기록이면 0 → AVERAGE 매핑(영웅 이미지는 AVERAGE)
        .body("data.heroImage", containsString("toilet/normal.png"))
        .body("data.heroBackgroundColors", hasItems("#2B42B4", "#8F58FF"));
  }
}
