package depromeet.lessonfour.server.toiletrecord.api.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

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
import depromeet.lessonfour.server.user.domain.entity.User;
import depromeet.lessonfour.server.user.domain.repository.UserRepository;
import io.restassured.RestAssured;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql(
    scripts = "/sql/cleanup.sql",
    config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED),
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class UpdateToiletRecordE2ETest {

  @LocalServerPort int port;

  @Autowired JwtTokenGenerator jwtTokenGenerator;
  @Autowired UserRepository userRepository;
  @Autowired PasswordEncoder passwordEncoder;

  private String token; // "Bearer xxx"
  private User me;

  private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

    me =
        userRepository.save(
            User.register(
                "toiletReport-update@example.com",
                "update-user",
                passwordEncoder.encode("pw1234")));
    token = "Bearer " + jwtTokenGenerator.generateAccessToken(AccountContext.of(me));
  }

  private int createToiletRecord(
      LocalDateTime occurredAt,
      boolean isSuccessful,
      int pain,
      int duration,
      String toiletColor,
      String toiletShape) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("occurredAt", occurredAt.format(DT));
    body.put("isSuccessful", isSuccessful);
    body.put("color", toiletColor);
    body.put("shape", toiletShape);
    body.put("pain", pain);
    body.put("duration", duration);

    return given()
        .header("Authorization", token)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(body)
        .when()
        .post("/api/v1/poo-records")
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("status", equalTo(201))
        .body("data.id", notNullValue())
        .extract()
        .path("data.id");
  }

  private int createSuccessfulToiletRecord(
      LocalDateTime occurredAt, int pain, int duration, String toiletColor, String toiletShape) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("occurredAt", occurredAt.format(DT));
    body.put("isSuccessful", true);
    body.put("color", toiletColor);
    body.put("shape", toiletShape);
    body.put("pain", pain);
    body.put("duration", duration);

    return given()
        .header("Authorization", token)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(body)
        .when()
        .post("/api/v1/poo-records")
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("status", equalTo(201))
        .body("data.id", notNullValue())
        .body("data.isSuccessful", equalTo(true))
        .body("data.color", equalTo(toiletColor))
        .body("data.shape", equalTo(toiletShape))
        .extract()
        .path("data.id");
  }

  private int createUnsuccessfulToiletRecord(LocalDateTime occurredAt, int pain, int duration) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("occurredAt", occurredAt.format(DT));
    body.put("isSuccessful", false);
    body.put("color", null);
    body.put("shape", null);
    body.put("pain", pain);
    body.put("duration", duration);

    return given()
        .header("Authorization", token)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(body)
        .when()
        .post("/api/v1/poo-records")
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("status", equalTo(201))
        .body("data.id", notNullValue())
        .body("data.isSuccessful", equalTo(false))
        .body("data.color", nullValue())
        .body("data.shape", nullValue())
        .extract()
        .path("data.id");
  }

  // ==== 생성 테스트: isSuccessful = true 케이스 ====
  @Test
  @DisplayName("성공적인 배변 기록 요청 시 color가 null이면 400 에러를 반환한다")
  void givenSuccessfulRecordRequest_whenColorIsNull_thenReturnsBadRequest() {
    // given
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("occurredAt", LocalDateTime.of(2024, 7, 1, 8, 30).format(DT));
    body.put("isSuccessful", true);
    body.put("color", null);
    body.put("shape", "BANANA");
    body.put("pain", 10);
    body.put("duration", 5);

    // when & then
    given()
        .header("Authorization", token)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(body)
        .when()
        .post("/api/v1/poo-records")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  @DisplayName("성공적인 배변 기록 요청 시 shape가 null이면 400 에러를 반환한다")
  void givenSuccessfulRecordRequest_whenShapeIsNull_thenReturnsBadRequest() {
    // given
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("occurredAt", LocalDateTime.of(2024, 7, 1, 8, 30).format(DT));
    body.put("isSuccessful", true);
    body.put("color", "GOLD");
    body.put("shape", null);
    body.put("pain", 10);
    body.put("duration", 5);

    // when & then
    given()
        .header("Authorization", token)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(body)
        .when()
        .post("/api/v1/poo-records")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  @DisplayName("성공적인 배변 기록 요청 시 color와 shape가 모두 null이면 400 에러를 반환한다")
  void givenSuccessfulRecordRequest_whenColorAndShapeAreNull_thenReturnsBadRequest() {
    // given
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("occurredAt", LocalDateTime.of(2024, 7, 1, 8, 30).format(DT));
    body.put("isSuccessful", true);
    body.put("color", null);
    body.put("shape", null);
    body.put("pain", 10);
    body.put("duration", 5);

    // when & then
    given()
        .header("Authorization", token)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(body)
        .when()
        .post("/api/v1/poo-records")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  @DisplayName("기존 배변 기록이 있을 때 note만 수정하면 부분 수정에 성공한다")
  void givenExistingRecord_whenPatchingNoteOnly_thenUpdatesSuccessfully() {
    // given
    int id =
        createSuccessfulToiletRecord(LocalDateTime.of(2024, 7, 3, 10, 0), 20, 10, "GOLD", "BANANA");
    var patchBody = Map.of("note", "after breakfast");

    // when & then
    given()
        .header("Authorization", token)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(patchBody)
        .when()
        .patch("/api/v1/poo-records/{id}", id)
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("status", equalTo(200))
        .body("data.id", equalTo(id))
        .body("data.note", equalTo("after breakfast"))
        .body("data.color", equalTo("GOLD"))
        .body("data.shape", equalTo("BANANA"));
  }

  @Test
  @DisplayName("기존 배변 기록이 있을 때 color와 shape를 수정하면 수정에 성공한다")
  void givenExistingRecord_whenPatchingColorAndShape_thenUpdatesSuccessfully() {
    // given
    int id =
        createSuccessfulToiletRecord(LocalDateTime.of(2024, 7, 3, 10, 0), 20, 10, "GOLD", "BANANA");
    var patchBody =
        Map.of(
            "color", "DEFAULT",
            "shape", "CORN");

    // when & then
    given()
        .header("Authorization", token)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(patchBody)
        .when()
        .patch("/api/v1/poo-records/{id}", id)
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("status", equalTo(200))
        .body("data.color", equalTo("DEFAULT"))
        .body("data.shape", equalTo("CORN"));
  }

  @Test
  @DisplayName("기존 배변 기록이 있을 때 유효하지 않은 color 값으로 수정하면 400 에러를 반환한다")
  void givenExistingRecord_whenPatchingWithInvalidColor_thenReturnsBadRequest() {
    // given
    int id =
        createSuccessfulToiletRecord(LocalDateTime.of(2024, 7, 4, 11, 0), 0, 5, "GOLD", "BANANA");
    var patchBody = Map.of("color", "NOT_A_COLOR");

    // when & then
    given()
        .header("Authorization", token)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(patchBody)
        .when()
        .patch("/api/v1/poo-records/{id}", id)
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  @DisplayName("기존 배변 기록이 있을 때 유효하지 않은 shape 값으로 수정하면 400 에러를 반환한다")
  void givenExistingRecord_whenPatchingWithInvalidShape_thenReturnsBadRequest() {
    // given
    int id =
        createSuccessfulToiletRecord(LocalDateTime.of(2024, 7, 4, 11, 0), 0, 5, "GOLD", "BANANA");
    var patchBody = Map.of("shape", "NOT_A_SHAPE");

    // when & then
    given()
        .header("Authorization", token)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(patchBody)
        .when()
        .patch("/api/v1/poo-records/{id}", id)
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  @DisplayName("존재하지 않는 기록 ID로 수정 요청하면 404 에러를 반환한다")
  void givenNonExistentRecordId_whenPatching_thenReturnsNotFound() {
    // given
    int nonExistentId = 99999;
    var patchBody = Map.of("note", "test note");

    // when & then
    given()
        .header("Authorization", token)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(patchBody)
        .when()
        .patch("/api/v1/poo-records/{id}", nonExistentId)
        .then()
        .statusCode(HttpStatus.NOT_FOUND.value());
  }

  @Test
  @DisplayName("다른 사용자의 기록을 수정하려고 하면 404 에러를 반환한다")
  void givenOtherUsersRecord_whenPatching_thenReturnsNotFound() {
    // given - 다른 사용자 생성
    User otherUser =
        userRepository.save(
            User.register("other@example.com", "other-user", passwordEncoder.encode("pw1234")));
    String otherToken =
        "Bearer " + jwtTokenGenerator.generateAccessToken(AccountContext.of(otherUser));

    // 다른 사용자의 기록 생성
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("occurredAt", LocalDateTime.of(2024, 7, 5, 10, 0).format(DT));
    body.put("isSuccessful", true);
    body.put("color", "GOLD");
    body.put("shape", "BANANA");
    body.put("pain", 10);
    body.put("duration", 5);

    int otherUserId =
        given()
            .header("Authorization", otherToken)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(body)
            .when()
            .post("/api/v1/poo-records")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .path("data.id");

    // when & then - 현재 사용자가 다른 사용자의 기록 수정 시도
    var patchBody = Map.of("note", "hacking attempt");

    given()
        .header("Authorization", token)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(patchBody)
        .when()
        .patch("/api/v1/poo-records/{id}", otherUserId)
        .then()
        .statusCode(HttpStatus.NOT_FOUND.value());
  }
}
