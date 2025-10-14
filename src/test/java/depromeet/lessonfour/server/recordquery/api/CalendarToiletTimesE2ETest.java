package depromeet.lessonfour.server.recordquery.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import depromeet.lessonfour.server.toiletrecord.domain.repository.ToiletRecordRepository;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletColor;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletShape;
import depromeet.lessonfour.server.user.domain.entity.User;
import depromeet.lessonfour.server.user.infra.repository.UserRepository;
import io.restassured.RestAssured;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql(
    scripts = "/sql/cleanup.sql",
    config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED),
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class CalendarToiletTimesE2ETest {

  @LocalServerPort int port;

  @Autowired JwtTokenGenerator jwtTokenGenerator;
  @Autowired UserRepository userRepository;
  @Autowired PasswordEncoder passwordEncoder;
  @Autowired ToiletRecordRepository toiletRecordRepository;

  private String validJwtToken;
  private User testUser;
  private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

    testUser = createTestUser("toilet-times@example.com", "password123", "toilet-user");
    validJwtToken = "Bearer " + jwtTokenGenerator.generateAccessToken(AccountContext.of(testUser));
  }

  private User createTestUser(String email, String password, String nickname) {
    User user = User.register(email, nickname, passwordEncoder.encode(password));
    return userRepository.save(user);
  }

  private ToiletRecord createToilet(LocalDateTime dt, User owner) {
    ToiletRecord r =
        ToiletRecord.register(
            owner,
            true,
            ToiletColor.DEFAULT,
            ToiletShape.BANANA,
            10,
            3,
            "note",
            ActivityAt.from(dt));
    toiletRecordRepository.save(r);
    return r;
  }

  @Test
  @DisplayName("특정 날짜의 배변 기록 시간(id 포함)을 오름차순으로 반환한다")
  void givenDate_whenGetToiletTimes_thenSortedListWithIds() {
    // Given
    LocalDate date = LocalDate.of(2024, 2, 1);
    var r1 = createToilet(LocalDateTime.of(2024, 2, 1, 18, 30), testUser);
    var r2 = createToilet(LocalDateTime.of(2024, 2, 1, 07, 15), testUser);
    var r3 = createToilet(LocalDateTime.of(2024, 2, 1, 12, 00), testUser);

    // When & Then
    given()
        .header("Authorization", validJwtToken)
        .queryParam("date", date.format(dateFormatter))
        .when()
        .get("/api/v1/calendar/toilets")
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.date", equalTo("2024-02-01"))
        .body("data.items", hasSize(3))
        // 오름차순 정렬 검증 (07:15:00, 12:00:00, 18:30:00)
        .body("data.items[0].activityTime", equalTo("07:15:00"))
        .body("data.items[1].activityTime", equalTo("12:00:00"))
        .body("data.items[2].activityTime", equalTo("18:30:00"))
        // id 포함 여부(값 자체는 실행마다 달라질 수 있어서 null 아님만 체크)
        .body("data.items[0].id", notNullValue())
        .body("data.items[1].id", notNullValue())
        .body("data.items[2].id", notNullValue());
  }

  @Test
  @DisplayName("삭제된 기록은 리스트에서 제외된다")
  void givenDeletedRecords_whenGetToiletTimes_thenExcluded() {
    // Given
    LocalDate date = LocalDate.of(2024, 3, 10);
    var kept = createToilet(LocalDateTime.of(2024, 3, 10, 9, 0), testUser);
    var deleted = createToilet(LocalDateTime.of(2024, 3, 10, 11, 0), testUser);
    deleted.delete();
    toiletRecordRepository.save(deleted);

    // When & Then
    given()
        .header("Authorization", validJwtToken)
        .queryParam("date", date.format(dateFormatter))
        .when()
        .get("/api/v1/calendar/toilets")
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("status", equalTo(200))
        .body("data.date", equalTo("2024-03-10"))
        .body("data.items", hasSize(1))
        .body("data.items[0].activityTime", equalTo("09:00:00"))
        .body("data.items[0].id", equalTo(kept.getId().intValue())); // JSON이 int로 내려오면 int 비교
  }

  @Test
  @DisplayName("다른 사용자의 기록은 포함되지 않는다")
  void givenOtherUserRecords_whenGetToiletTimes_thenNotIncluded() {
    // Given
    LocalDate date = LocalDate.of(2024, 4, 2);
    // 내 기록
    createToilet(LocalDateTime.of(2024, 4, 2, 8, 0), testUser);
    // 다른 사용자 기록
    User other = createTestUser("other@example.com", "pw", "other");
    createToilet(LocalDateTime.of(2024, 4, 2, 9, 0), other);

    // When & Then
    given()
        .header("Authorization", validJwtToken)
        .queryParam("date", date.format(dateFormatter))
        .when()
        .get("/api/v1/calendar/toilets")
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("status", equalTo(200))
        .body("data.items", hasSize(1))
        .body("data.items[0].activityTime", equalTo("08:00:00"));
  }

  @Test
  @DisplayName("해당 날짜에 기록이 없으면 빈 리스트를 반환한다")
  void givenNoRecords_whenGetToiletTimes_thenEmptyList() {
    // Given
    LocalDate date = LocalDate.of(2024, 5, 5);

    // When & Then
    given()
        .header("Authorization", validJwtToken)
        .queryParam("date", date.format(dateFormatter))
        .when()
        .get("/api/v1/calendar/toilets")
        .then()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("data.date", equalTo("2024-05-05"))
        .body("data.items", hasSize(0));
  }

  @Test
  @DisplayName("JWT 토큰 없이 요청 시 401")
  void givenNoToken_whenGetToiletTimes_thenUnauthorized() {
    LocalDate date = LocalDate.of(2024, 6, 1);

    given()
        .queryParam("date", date.format(dateFormatter))
        .when()
        .get("/api/v1/calendar/toilets")
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  @DisplayName("잘못된 토큰이면 401")
  void givenInvalidToken_whenGetToiletTimes_thenUnauthorized() {
    LocalDate date = LocalDate.of(2024, 6, 1);

    given()
        .header("Authorization", "Bearer invalid-token")
        .queryParam("date", date.format(dateFormatter))
        .when()
        .get("/api/v1/calendar/toilets")
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  @DisplayName("date 파라미터 없으면 400")
  void givenNoDate_whenGetToiletTimes_thenBadRequest() {
    given()
        .header("Authorization", validJwtToken)
        .when()
        .get("/api/v1/calendar/toilets")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  @DisplayName("날짜 형식이 잘못되면 400")
  void givenInvalidDateFormat_whenGetToiletTimes_thenBadRequest() {
    given()
        .header("Authorization", validJwtToken)
        .queryParam("date", "not-a-date")
        .when()
        .get("/api/v1/calendar/toilets")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value());
  }
}
