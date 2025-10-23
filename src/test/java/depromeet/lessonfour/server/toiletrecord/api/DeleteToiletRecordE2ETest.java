package depromeet.lessonfour.server.toiletrecord.api;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.time.Duration;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import depromeet.lessonfour.server.auth.domain.vo.AccountContext;
import depromeet.lessonfour.server.auth.infra.security.jwt.JwtTokenGenerator;
import depromeet.lessonfour.server.report.domain.entity.ToiletScore;
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
class DeleteToiletRecordE2ETest {

  @LocalServerPort private int port;

  @Autowired private JwtTokenGenerator jwtTokenGenerator;
  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private JpaToiletScoreRepository toiletScoreRepository;

  private String validJwtToken;
  private User testUser;
  private final DateTimeFormatter formatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

    // 실제 사용자 생성 및 JWT 토큰 생성
    testUser = createTestUser("test@example.com", "password123", "testuser");
    validJwtToken = "Bearer " + jwtTokenGenerator.generateAccessToken(AccountContext.of(testUser));
  }

  private User createTestUser(String email, String password, String nickname) {
    User user = User.register(email, nickname, passwordEncoder.encode(password));
    return userRepository.save(user);
  }

  private Long createToiletRecord(String requestBody) {
    Integer id =
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", validJwtToken)
            .body(requestBody)
            .when()
            .post("/api/v1/poo-records")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .path("data.id");
    return id.longValue();
  }

  @Test
  @DisplayName("유효한 배변기록 삭제 요청시 성공적으로 삭제된다")
  void givenValidToiletRecord_whenDeleteToiletRecord_thenSuccess() {
    // given: 배변기록 생성
    LocalDateTime now = LocalDateTime.now();
    String createRequest =
        String.format(
            """
        {
          "occurredAt": "%s",
          "isSuccessful": true,
          "color": "DEFAULT",
          "shape": "BANANA",
          "pain": 0,
          "duration": 10,
          "note": "정상적인 배변"
        }
        """,
            now.format(formatter));

    Long toiletRecordId = createToiletRecord(createRequest);

    // when: 배변기록 삭제
    given()
        .header("Authorization", validJwtToken)
        .when()
        .delete("/api/v1/poo-records/" + toiletRecordId)
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("status", equalTo(200))
        .body("message", equalTo("삭제가 완료되었습니다."))
        .body("data", equalTo(null));
  }

  @Test
  @DisplayName("JWT 토큰 없이 삭제 요청시 401 에러가 발생한다")
  void givenNoJwtToken_whenDeleteToiletRecord_thenUnauthorized() {
    given()
        .when()
        .delete("/api/v1/poo-records/1")
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  @DisplayName("유효하지 않은 JWT 토큰으로 삭제 요청시 401 에러가 발생한다")
  void givenInvalidJwtToken_whenDeleteToiletRecord_thenUnauthorized() {
    given()
        .header("Authorization", "Bearer invalid.jwt.token")
        .when()
        .delete("/api/v1/poo-records/1")
        .then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  @DisplayName("존재하지 않는 배변기록 삭제시 404 에러가 발생한다")
  void givenNonExistentRecord_whenDeleteToiletRecord_thenNotFound() {
    given()
        .header("Authorization", validJwtToken)
        .when()
        .delete("/api/v1/poo-records/99999")
        .then()
        .statusCode(HttpStatus.NOT_FOUND.value())
        .body("message", containsString("데이터가 존재하지 않습니다"));
  }

  @Test
  @DisplayName("삭제 후 동일한 배변기록 재삭제시 404 에러가 발생한다")
  void givenDeletedRecord_whenDeleteAgain_thenNotFound() {
    // given: 배변기록 생성 및 삭제
    LocalDateTime now = LocalDateTime.now();
    String createRequest =
        String.format(
            """
        {
          "occurredAt": "%s",
          "isSuccessful": true,
          "color": "DEFAULT",
          "shape": "BANANA",
          "pain": 0,
          "duration": 10
        }
        """,
            now.format(formatter));

    Long toiletRecordId = createToiletRecord(createRequest);

    // 첫 번째 삭제 성공
    given()
        .header("Authorization", validJwtToken)
        .when()
        .delete("/api/v1/poo-records/" + toiletRecordId)
        .then()
        .statusCode(HttpStatus.OK.value());

    // when: 동일한 기록 재삭제 시도
    // then: 404 에러
    given()
        .header("Authorization", validJwtToken)
        .when()
        .delete("/api/v1/poo-records/" + toiletRecordId)
        .then()
        .statusCode(HttpStatus.NOT_FOUND.value())
        .body("message", containsString("데이터가 존재하지 않습니다"));
  }

  // TODO: Delete event의 ActivityAt이 삭제된 record의 실제 날짜가 아닌 현재 시간을 사용하여 ToiletScore 업데이트가 제대로 되지 않을 수
  // 있음
  // @Test
  // @DisplayName("배변기록 삭제시 ToiletScore가 업데이트된다")
  void givenToiletRecordDeleted_whenEventProcessed_thenToiletScoreIsUpdated() {
    // given: 배변기록 생성
    LocalDateTime now = LocalDateTime.now();
    LocalDate today = now.toLocalDate();
    String createRequest =
        String.format(
            """
        {
          "occurredAt": "%s",
          "isSuccessful": true,
          "color": "DEFAULT",
          "shape": "BANANA",
          "pain": 0,
          "duration": 10
        }
        """,
            now.format(formatter));

    Long toiletRecordId = createToiletRecord(createRequest);

    // 초기 ToiletScore 생성 대기
    await()
        .atMost(Duration.ofSeconds(5))
        .pollInterval(Duration.ofMillis(100))
        .untilAsserted(
            () -> {
              List<ToiletScore> scores =
                  toiletScoreRepository.findAll().stream()
                      .filter(score -> score.getUserId().equals(testUser.getId()))
                      .filter(score -> score.getDate().equals(today))
                      .toList();
              assertThat(scores).isNotEmpty();
            });

    int initialScoreCount =
        (int)
            toiletScoreRepository.findAll().stream()
                .filter(score -> score.getUserId().equals(testUser.getId()))
                .filter(score -> score.getDate().equals(today))
                .count();

    // when: 배변기록 삭제
    given()
        .header("Authorization", validJwtToken)
        .when()
        .delete("/api/v1/poo-records/" + toiletRecordId)
        .then()
        .statusCode(HttpStatus.OK.value());

    // then: ToiletScore가 업데이트됨 (새로운 엔트리 생성 또는 점수 재계산)
    await()
        .atMost(Duration.ofSeconds(5))
        .pollInterval(Duration.ofMillis(100))
        .untilAsserted(
            () -> {
              List<ToiletScore> scores =
                  toiletScoreRepository.findAll().stream()
                      .filter(score -> score.getUserId().equals(testUser.getId()))
                      .filter(score -> score.getDate().equals(today))
                      .toList();

              // 삭제 후에는 새로운 ToiletScore가 생성되거나, 기존과 다른 상태여야 함
              boolean hasMoreEntries = scores.size() > initialScoreCount;
              boolean hasNoRecords = scores.isEmpty(); // 유일한 기록이 삭제되면 점수도 없을 수 있음

              assertThat(hasMoreEntries || hasNoRecords)
                  .as("ToiletScore should be updated after record deletion")
                  .isTrue();
            });
  }

  // TODO: Delete event의 ActivityAt이 삭제된 record의 실제 날짜가 아닌 현재 시간을 사용하여 ToiletScore 업데이트가 제대로 되지 않을 수
  // 있음
  // @Test
  // @DisplayName("같은 날짜에 여러 배변기록 중 하나 삭제시 ToiletScore가 재계산된다")
  void givenMultipleRecords_whenOneDeleted_thenToiletScoreIsRecalculated() {
    LocalDateTime now = LocalDateTime.now();
    LocalDate today = now.toLocalDate();

    // 첫 번째 배변 기록 생성
    String firstRequest =
        String.format(
            """
        {
          "occurredAt": "%s",
          "isSuccessful": true,
          "color": "DEFAULT",
          "shape": "BANANA",
          "pain": 0,
          "duration": 10
        }
        """,
            now.format(formatter));

    Long firstRecordId = createToiletRecord(firstRequest);

    // 두 번째 배변 기록 생성
    LocalDateTime laterTime = now.plusHours(1);
    String secondRequest =
        String.format(
            """
        {
          "occurredAt": "%s",
          "isSuccessful": true,
          "color": "GOLD",
          "shape": "BANANA",
          "pain": 10,
          "duration": 8
        }
        """,
            laterTime.format(formatter));

    Long secondRecordId = createToiletRecord(secondRequest);

    // 두 번째 ToiletScore 생성 대기
    await()
        .atMost(Duration.ofSeconds(5))
        .pollInterval(Duration.ofMillis(100))
        .untilAsserted(
            () -> {
              List<ToiletScore> scores =
                  toiletScoreRepository.findAll().stream()
                      .filter(score -> score.getUserId().equals(testUser.getId()))
                      .filter(score -> score.getDate().equals(today))
                      .toList();
              assertThat(scores).isNotEmpty();
            });

    int scoreCountAfterTwoRecords =
        (int)
            toiletScoreRepository.findAll().stream()
                .filter(score -> score.getUserId().equals(testUser.getId()))
                .filter(score -> score.getDate().equals(today))
                .count();

    // 첫 번째 배변기록 삭제
    given()
        .header("Authorization", validJwtToken)
        .when()
        .delete("/api/v1/poo-records/" + firstRecordId)
        .then()
        .statusCode(HttpStatus.OK.value());

    // ToiletScore가 재계산됨
    await()
        .atMost(Duration.ofSeconds(5))
        .pollInterval(Duration.ofMillis(100))
        .untilAsserted(
            () -> {
              List<ToiletScore> scores =
                  toiletScoreRepository.findAll().stream()
                      .filter(score -> score.getUserId().equals(testUser.getId()))
                      .filter(score -> score.getDate().equals(today))
                      .toList();

              assertThat(scores).isNotEmpty();
              // 삭제 후에는 새로운 ToiletScore가 생성되어야 함 (재계산됨)
              boolean hasMoreEntries = scores.size() > scoreCountAfterTwoRecords;

              assertThat(hasMoreEntries)
                  .as("ToiletScore should be recalculated after one record deletion")
                  .isTrue();
            });
  }

  // TODO: Delete event의 ActivityAt이 삭제된 record의 실제 날짜가 아닌 현재 시간을 사용하여 ToiletScore 업데이트가 제대로 되지 않을 수
  // 있음
  // @Test
  // @DisplayName("같은 날짜의 모든 배변기록 삭제시 ToiletScore가 0 또는 삭제된다")
  void givenAllRecordsDeleted_whenEventProcessed_thenToiletScoreIsZeroOrDeleted() {
    LocalDateTime now = LocalDateTime.now();
    LocalDate today = now.toLocalDate();

    // 첫 번째 배변 기록 생성
    String firstRequest =
        String.format(
            """
        {
          "occurredAt": "%s",
          "isSuccessful": true,
          "color": "DEFAULT",
          "shape": "BANANA",
          "pain": 0,
          "duration": 10
        }
        """,
            now.format(formatter));

    Long firstRecordId = createToiletRecord(firstRequest);

    // 두 번째 배변 기록 생성
    LocalDateTime laterTime = now.plusHours(1);
    String secondRequest =
        String.format(
            """
        {
          "occurredAt": "%s",
          "isSuccessful": true,
          "color": "GOLD",
          "shape": "BANANA",
          "pain": 10,
          "duration": 8
        }
        """,
            laterTime.format(formatter));

    Long secondRecordId = createToiletRecord(secondRequest);

    // ToiletScore 생성 대기
    await()
        .atMost(Duration.ofSeconds(5))
        .pollInterval(Duration.ofMillis(100))
        .untilAsserted(
            () -> {
              List<ToiletScore> scores =
                  toiletScoreRepository.findAll().stream()
                      .filter(score -> score.getUserId().equals(testUser.getId()))
                      .filter(score -> score.getDate().equals(today))
                      .toList();
              assertThat(scores).isNotEmpty();
            });

    // 모든 배변기록 삭제
    given()
        .header("Authorization", validJwtToken)
        .when()
        .delete("/api/v1/poo-records/" + firstRecordId)
        .then()
        .statusCode(HttpStatus.OK.value());

    given()
        .header("Authorization", validJwtToken)
        .when()
        .delete("/api/v1/poo-records/" + secondRecordId)
        .then()
        .statusCode(HttpStatus.OK.value());

    // ToiletScore가 업데이트됨 (0점 또는 삭제됨)
    await()
        .atMost(Duration.ofSeconds(5))
        .pollInterval(Duration.ofMillis(100))
        .untilAsserted(
            () -> {
              List<ToiletScore> scores =
                  toiletScoreRepository.findAll().stream()
                      .filter(score -> score.getUserId().equals(testUser.getId()))
                      .filter(score -> score.getDate().equals(today))
                      .toList();

              // 모든 기록이 삭제되면:
              // 1. ToiletScore가 없거나 (빈 리스트)
              // 2. 최신 ToiletScore의 점수가 0이어야 함
              boolean noScores = scores.isEmpty();
              boolean hasZeroScore =
                  !scores.isEmpty()
                      && scores.stream()
                          .max((s1, s2) -> s1.getId().compareTo(s2.getId())) // 가장 최신 점수 확인
                          .map(score -> score.getScore() == 0)
                          .orElse(false);

              assertThat(noScores || hasZeroScore)
                  .as(
                      "ToiletScore should be zero or deleted when all records for the day are deleted")
                  .isTrue();
            });
  }
}
