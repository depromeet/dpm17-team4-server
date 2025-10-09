package depromeet.lessonfour.server.activityrecord.controllers;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.time.LocalDateTime;
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
import depromeet.lessonfour.server.user.domain.entity.User;
import depromeet.lessonfour.server.user.infra.repository.UserRepository;
import io.restassured.RestAssured;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql(
    scripts = "/sql/cleanup.sql",
    config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED),
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class DeleteActivityRecordE2ETest {

  @LocalServerPort private int port;

  @Autowired private JwtTokenGenerator jwtTokenGenerator;
  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private FoodRepository foodRepository;
  @Autowired private JpaActivityRecordRepository activityRecordRepository;

  private String validJwtToken;
  private String anotherUserJwtToken;
  private Long testUserId;
  private Long anotherUserId;
  private Food testFood;
  private ActivityRecord testActivityRecord;

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

    // 테스트용 사용자들 생성
    testUserId = createTestUser("test@example.com", "password123", "testuser");
    anotherUserId = createTestUser("another@example.com", "password456", "anotheruser");

    User testUser = userRepository.findById(testUserId).orElseThrow();
    User anotherUser = userRepository.findById(anotherUserId).orElseThrow();

    validJwtToken = "Bearer " + jwtTokenGenerator.generateAccessToken(AccountContext.of(testUser));
    anotherUserJwtToken =
        "Bearer " + jwtTokenGenerator.generateAccessToken(AccountContext.of(anotherUser));

    // 테스트용 Food 데이터 생성
    testFood = Food.builder().name("사과").score(4.5).build();
    foodRepository.save(testFood);

    // 테스트용 ActivityRecord 생성
    testActivityRecord = createTestActivityRecord(testUserId);
  }

  private Long createTestUser(String email, String password, String nickname) {
    User user = User.register(email, nickname, passwordEncoder.encode(password));
    return userRepository.save(user).getId();
  }

  private ActivityRecord createTestActivityRecord(Long userId) {
    List<MealFood> mealFoods = List.of(new MealFood(MealTime.BREAKFAST, testFood));

    ActivityRecord activityRecord =
        ActivityRecord.createWithMeals(
            userId, 5, StressLevel.MEDIUM, ActivityAt.from(LocalDateTime.now()), mealFoods);

    return activityRecordRepository.save(activityRecord);
  }

  private ActivityRecord createTestActivityRecordWithDate(Long userId, LocalDateTime dateTime) {
    List<MealFood> mealFoods = List.of(new MealFood(MealTime.BREAKFAST, testFood));

    ActivityRecord activityRecord =
        ActivityRecord.createWithMeals(
            userId, 5, StressLevel.MEDIUM, ActivityAt.from(dateTime), mealFoods);

    return activityRecordRepository.save(activityRecord);
  }

  @Test
  @DisplayName("유효한 생활 기록 삭제 요청시 성공적으로 삭제된다")
  void givenValidActivityRecordDeleteRequest_whenDeleteActivityRecord_thenSuccess() {
    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .when()
        .delete("/api/v1/activity-records/{activityRecordId}", testActivityRecord.getId())
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.OK.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("status", equalTo(200))
        .body("message", equalTo("삭제가 완료되었습니다."));
  }

  @Test
  @DisplayName("JWT 토큰 없이 삭제 요청시 401 에러를 반환한다")
  void givenNoAuthToken_whenDeleteActivityRecord_thenUnauthorized() {
    given()
        .log()
        .all()
        .when()
        .delete("/api/v1/activity-records/{activityRecordId}", testActivityRecord.getId())
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  @DisplayName("유효하지 않은 JWT 토큰으로 삭제 요청시 401 에러를 반환한다")
  void givenInvalidAuthToken_whenDeleteActivityRecord_thenUnauthorized() {
    given()
        .log()
        .all()
        .header("Authorization", "Bearer invalid-token")
        .when()
        .delete("/api/v1/activity-records/{activityRecordId}", testActivityRecord.getId())
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  @DisplayName("다른 사용자의 생활 기록 삭제 요청시 403 에러를 반환한다")
  void givenAnotherUserActivityRecord_whenDeleteActivityRecord_thenNotFound() {
    given()
        .log()
        .all()
        .header("Authorization", anotherUserJwtToken)
        .when()
        .delete("/api/v1/activity-records/{activityRecordId}", testActivityRecord.getId())
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.NOT_FOUND.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("message", containsString("데이터가 존재하지 않습니다"));
  }

  @Test
  @DisplayName("존재하지 않는 생활 기록 삭제 요청시 404 에러를 반환한다")
  void givenNonExistentActivityRecord_whenDeleteActivityRecord_thenNotFound() {
    Long nonExistentId = 99999L;

    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .when()
        .delete("/api/v1/activity-records/{activityRecordId}", nonExistentId)
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.NOT_FOUND.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("message", containsString("데이터가 존재하지 않습니다"));
  }

  @Test
  @DisplayName("이미 삭제된 생활 기록 삭제 요청시 404 에러를 반환한다")
  void givenAlreadyDeletedActivityRecord_whenDeleteActivityRecord_thenNotFound() {
    // 먼저 생활 기록을 삭제
    testActivityRecord.delete();
    activityRecordRepository.save(testActivityRecord);

    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .when()
        .delete("/api/v1/activity-records/{activityRecordId}", testActivityRecord.getId())
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.NOT_FOUND.value())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body("message", containsString("데이터가 존재하지 않습니다"));
  }

  @Test
  @DisplayName("유효하지 않은 ActivityRecord ID 형식으로 요청시 400 에러를 반환한다")
  void givenInvalidActivityRecordIdFormat_whenDeleteActivityRecord_thenBadRequest() {
    given()
        .log()
        .all()
        .header("Authorization", validJwtToken)
        .when()
        .delete("/api/v1/activity-records/{activityRecordId}", "invalid-id")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  @DisplayName("삭제 후 소프트 삭제 상태를 확인한다")
  void givenSuccessfulDelete_whenCheckActivityRecord_thenSoftDeleted() {
    // 삭제 요청 수행
    given()
        .header("Authorization", validJwtToken)
        .when()
        .delete("/api/v1/activity-records/{activityRecordId}", testActivityRecord.getId())
        .then()
        .statusCode(HttpStatus.OK.value());

    // 데이터베이스에서 삭제 상태 확인
    ActivityRecord deletedRecord =
        activityRecordRepository
            .findById(testActivityRecord.getId())
            .orElseThrow(
                () -> new RuntimeException("ActivityRecord should still exist in database"));

    assert deletedRecord.isDeleted() : "ActivityRecord should be marked as deleted";

    // 조회 시 삭제된 기록은 찾을 수 없어야 함
    assertThat(
            activityRecordRepository.findByUserIdAndIdAndIsDeletedFalse(
                testUserId, testActivityRecord.getId()))
        .isEmpty();
  }

  @Test
  @DisplayName("여러 생활 기록 중 특정 기록만 삭제된다")
  void givenMultipleActivityRecords_whenDeleteOne_thenOnlyTargetDeleted() {
    // 추가 생활 기록 생성
    ActivityRecord anotherActivityRecord =
        createTestActivityRecordWithDate(testUserId, LocalDateTime.now().minusDays(1));

    // 첫 번째 기록 삭제
    given()
        .header("Authorization", validJwtToken)
        .when()
        .delete("/api/v1/activity-records/{activityRecordId}", testActivityRecord.getId())
        .then()
        .statusCode(HttpStatus.OK.value());

    // 첫 번째 기록은 삭제 상태, 두 번째 기록은 유지 상태 확인
    ActivityRecord firstRecord =
        activityRecordRepository.findById(testActivityRecord.getId()).orElseThrow();
    ActivityRecord secondRecord =
        activityRecordRepository.findById(anotherActivityRecord.getId()).orElseThrow();

    assertThat(firstRecord.isDeleted()).isTrue();
    assertThat(secondRecord.isDeleted()).isFalse();
  }
}
