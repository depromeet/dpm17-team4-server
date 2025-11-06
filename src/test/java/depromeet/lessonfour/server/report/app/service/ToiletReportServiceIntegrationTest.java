package depromeet.lessonfour.server.report.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.report.domain.entity.ToiletScore;
import depromeet.lessonfour.server.report.domain.repository.ToiletScoreRepository;
import depromeet.lessonfour.server.report.domain.vo.monthly.MonthlyToiletReport;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import depromeet.lessonfour.server.toiletrecord.domain.repository.ToiletRecordRepository;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletColor;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletShape;
import depromeet.lessonfour.server.user.domain.entity.User;
import depromeet.lessonfour.server.user.domain.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
@Sql(
    scripts = "/sql/cleanup.sql",
    config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED),
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ToiletReportServiceIntegrationTest {

  @Autowired private ToiletReportService toiletReportService;
  @Autowired private ToiletScoreRepository toiletScoreRepository;
  @Autowired private ToiletRecordRepository toiletRecordRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  private User testUser;
  private Long testUserId;

  @BeforeEach
  void setUp() {
    testUser = createTestUser("toiletreport@example.com", "password123", "toilet-test-user");
    testUserId = testUser.getId();
  }

  private User createTestUser(String email, String password, String nickname) {
    User user = User.register(email, nickname, passwordEncoder.encode(password));
    return userRepository.save(user);
  }

  private ToiletRecord createToiletRecord(
      LocalDateTime dateTime,
      Boolean isSuccessful,
      ToiletColor color,
      ToiletShape shape,
      Integer pain,
      Integer duration,
      String note) {
    ToiletRecord record =
        ToiletRecord.register(
            testUser,
            isSuccessful != null ? isSuccessful : true,
            color, // nullable
            shape, // nullable
            pain != null ? pain : 10,
            duration != null ? duration : 5,
            note, // nullable
            ActivityAt.from(dateTime));
    toiletRecordRepository.save(record);
    return record;
  }

  private ToiletScore createToiletScore(LocalDate date, int score) {
    ToiletScore toiletScore = ToiletScore.of(testUserId, score, date);
    return toiletScoreRepository.save(toiletScore);
  }

  @Test
  @DisplayName("모든 필드가 채워진 ToiletRecord 여러 건으로 월간 리포트 생성이 성공한다")
  void givenValidToiletRecords_whenGenerateMonthlyReport_thenSuccess() {
    // Given
    LocalDateTime baseDate = LocalDateTime.of(2025, 10, 15, 10, 0);
    ActivityAt start = ActivityAt.of(LocalDate.of(2025, 10, 1));
    ActivityAt end = ActivityAt.of(LocalDate.of(2025, 11, 1));

    // 현재 달 데이터
    createToiletRecord(baseDate, true, ToiletColor.DEFAULT, ToiletShape.BANANA, 10, 5, "정상 기록 1");
    createToiletRecord(
        baseDate.plusDays(5), true, ToiletColor.DEFAULT, ToiletShape.CREAM, 15, 7, "정상 기록 2");
    createToiletRecord(
        baseDate.plusDays(10), true, ToiletColor.GOLD, ToiletShape.CORN, 5, 3, "정상 기록 3");

    // 지난 달 데이터
    createToiletRecord(
        baseDate.minusMonths(1), true, ToiletColor.DARK_BROWN, ToiletShape.ROCK, 20, 10, "지난 달 기록");

    // 점수 데이터
    createToiletScore(LocalDate.of(2025, 10, 15), 85);
    createToiletScore(LocalDate.of(2025, 10, 20), 90);
    createToiletScore(LocalDate.of(2025, 10, 10), 75);

    // When
    MonthlyToiletReport report =
        assertDoesNotThrow(() -> toiletReportService.generateMonthlyReport(testUserId, start, end));

    // Then
    assertThat(report).isNotNull();
    assertThat(report.size()).isEqualTo(3);
    assertThat(report.scoreSummary()).isNotNull();
    assertThat(report.scoreSummary().best()).isNotNull();
    assertThat(report.scoreSummary().worst()).isNotNull();
    assertThat(report.shapeCount()).isNotEmpty();
    assertThat(report.colorCount()).isNotEmpty();
    assertThat(report.timeDistribution()).isNotNull();
    assertThat(report.painDistribution()).isNotNull();
    assertThat(report.periodCount()).isNotEmpty();
  }

  @Test
  @DisplayName("ToiletRecord가 없는 경우 빈 리스트로 처리되어 리포트 생성이 성공한다")
  void givenNoToiletRecords_whenGenerateMonthlyReport_thenNoNPE() {
    // Given
    ActivityAt start = ActivityAt.of(LocalDate.of(2025, 10, 1));
    ActivityAt end = ActivityAt.of(LocalDate.of(2025, 11, 1));

    // When
    MonthlyToiletReport report =
        assertDoesNotThrow(() -> toiletReportService.generateMonthlyReport(testUserId, start, end));

    // Then
    assertThat(report).isNotNull();
    assertThat(report.size()).isZero();
    assertThat(report.scoreSummary()).isNotNull();
    assertThat(report.scoreSummary().best()).isNull();
    assertThat(report.scoreSummary().worst()).isNull();
    assertThat(report.shapeCount()).isEmpty();
    assertThat(report.colorCount()).isEmpty();
    assertThat(report.timeDistribution()).isNotNull();
    assertThat(report.painDistribution()).isNotNull();
    // periodCount는 빈 레코드에 대해서도 모든 period를 0으로 반환할 수 있음
    assertThat(report.periodCount()).isNotNull();
  }

  @Test
  @DisplayName("color가 모두 null인 ToiletRecord만 주어지는 경우 리포트 생성에 성공한다")
  void givenToiletRecordsWithNullColor_whenGenerateMonthlyReport_thenNoNPE() {
    // Given
    LocalDateTime baseDate = LocalDateTime.of(2025, 10, 15, 10, 0);
    ActivityAt start = ActivityAt.of(LocalDate.of(2025, 10, 1));
    ActivityAt end = ActivityAt.of(LocalDate.of(2025, 11, 1));

    createToiletRecord(baseDate, true, null, ToiletShape.BANANA, 10, 5, "색상 null");
    createToiletRecord(baseDate.plusDays(1), true, null, ToiletShape.CREAM, 15, 7, "색상 null 2");

    // When & Then
    MonthlyToiletReport report =
        assertDoesNotThrow(() -> toiletReportService.generateMonthlyReport(testUserId, start, end));

    assertThat(report).isNotNull();
    assertThat(report.size()).isEqualTo(2);
    // color가 null인 경우 필터링되어 빈 리스트가 반환됨
    assertThat(report.colorCount()).isEmpty();
  }

  @Test
  @DisplayName("shape가 모두 null인 ToiletRecord만 주어지는 경우 리포트 생성에 성공한다")
  void givenToiletRecordsWithNullShape_whenGenerateMonthlyReport_thenNoNPE() {
    // Given
    LocalDateTime baseDate = LocalDateTime.of(2025, 10, 15, 10, 0);
    ActivityAt start = ActivityAt.of(LocalDate.of(2025, 10, 1));
    ActivityAt end = ActivityAt.of(LocalDate.of(2025, 11, 1));

    createToiletRecord(baseDate, true, ToiletColor.DEFAULT, null, 10, 5, "모양 null");
    createToiletRecord(baseDate.plusDays(1), true, ToiletColor.GOLD, null, 15, 7, "모양 null 2");

    // When & Then
    MonthlyToiletReport report =
        assertDoesNotThrow(() -> toiletReportService.generateMonthlyReport(testUserId, start, end));

    assertThat(report).isNotNull();
    assertThat(report.size()).isEqualTo(2);
    // shape가 null인 경우 필터링되어 빈 리스트가 반환됨
    assertThat(report.shapeCount()).isEmpty();
  }

  @Test
  @DisplayName("color와 shape 모두 null인 ToiletRecord만 주어지는 경우 리포트 생성에 성공한다")
  void givenToiletRecordsWithBothNullColorAndShape_whenGenerateMonthlyReport_thenNoNPE() {
    // Given
    LocalDateTime baseDate = LocalDateTime.of(2025, 10, 15, 10, 0);
    ActivityAt start = ActivityAt.of(LocalDate.of(2025, 10, 1));
    ActivityAt end = ActivityAt.of(LocalDate.of(2025, 11, 1));

    createToiletRecord(baseDate, true, null, null, 10, 5, "color와 shape 모두 null");
    createToiletRecord(baseDate.plusDays(1), false, null, null, 20, 8, "실패 케이스 모두 null");
    createToiletRecord(baseDate.plusDays(2), true, null, null, 5, 3, null);

    // When & Then
    MonthlyToiletReport report =
        assertDoesNotThrow(() -> toiletReportService.generateMonthlyReport(testUserId, start, end));

    assertThat(report).isNotNull();
    assertThat(report.size()).isEqualTo(3);
    // color와 shape 모두 null인 경우 필터링되어 빈 리스트가 반환됨
    assertThat(report.colorCount()).isEmpty();
    assertThat(report.shapeCount()).isEmpty();
  }

  @Test
  @DisplayName("note가 null인 ToiletRecord만 주어지는 경우 리포트 생성에 성공한다")
  void givenToiletRecordsWithNullNote_whenGenerateMonthlyReport_thenNoNPE() {
    // Given
    LocalDateTime baseDate = LocalDateTime.of(2025, 10, 15, 10, 0);
    ActivityAt start = ActivityAt.of(LocalDate.of(2025, 10, 1));
    ActivityAt end = ActivityAt.of(LocalDate.of(2025, 11, 1));

    createToiletRecord(baseDate, true, ToiletColor.DEFAULT, ToiletShape.BANANA, 10, 5, null);
    createToiletRecord(
        baseDate.plusDays(1), true, ToiletColor.GOLD, ToiletShape.CREAM, 15, 7, null);

    // When & Then
    MonthlyToiletReport report =
        assertDoesNotThrow(() -> toiletReportService.generateMonthlyReport(testUserId, start, end));

    assertThat(report).isNotNull();
    assertThat(report.size()).isEqualTo(2);
  }

  @Test
  @DisplayName("isSuccessful이 false인 ToiletRecord만 주어지는 경우 리포트 생성에 성공한다")
  void givenToiletRecordsWithFailure_whenGenerateMonthlyReport_thenNoNPE() {
    // Given
    LocalDateTime baseDate = LocalDateTime.of(2025, 10, 15, 10, 0);
    ActivityAt start = ActivityAt.of(LocalDate.of(2025, 10, 1));
    ActivityAt end = ActivityAt.of(LocalDate.of(2025, 11, 1));

    createToiletRecord(baseDate, false, ToiletColor.DEFAULT, ToiletShape.ROCK, 30, 12, "실패 케이스");
    createToiletRecord(
        baseDate.plusDays(1), false, ToiletColor.DARK_BROWN, ToiletShape.ROCK, 35, 15, "실패 케이스 2");
    createToiletRecord(
        baseDate.plusDays(2), true, ToiletColor.DEFAULT, ToiletShape.BANANA, 10, 5, "정상 케이스");

    // When & Then
    MonthlyToiletReport report =
        assertDoesNotThrow(() -> toiletReportService.generateMonthlyReport(testUserId, start, end));

    assertThat(report).isNotNull();
    assertThat(report.size()).isEqualTo(3);
    assertThat(report.painDistribution()).isNotNull();
  }

  @Test
  @DisplayName("다양한 null 조합의 ToiletRecord가 주어지는 경우 리포트 생성에 성공한다")
  void givenMixedToiletRecordsWithVariousNulls_whenGenerateMonthlyReport_thenNoNPE() {
    // Given
    LocalDateTime baseDate = LocalDateTime.of(2025, 10, 15, 10, 0);
    ActivityAt start = ActivityAt.of(LocalDate.of(2025, 10, 1));
    ActivityAt end = ActivityAt.of(LocalDate.of(2025, 11, 1));

    // 정상 케이스
    createToiletRecord(baseDate, true, ToiletColor.DEFAULT, ToiletShape.BANANA, 10, 5, "정상 기록");
    // color null
    createToiletRecord(baseDate.plusDays(1), true, null, ToiletShape.CREAM, 15, 7, "color null");
    // shape null
    createToiletRecord(baseDate.plusDays(2), true, ToiletColor.GOLD, null, 5, 3, "shape null");
    // 둘 다 null
    createToiletRecord(baseDate.plusDays(3), true, null, null, 20, 8, "둘 다 null");
    // 실패 + null
    createToiletRecord(baseDate.plusDays(4), false, null, null, 30, 12, "실패 + null");
    // note null
    createToiletRecord(
        baseDate.plusDays(5), true, ToiletColor.DARK_BROWN, ToiletShape.PORRIDGE, 10, 5, null);

    // When & Then
    MonthlyToiletReport report =
        assertDoesNotThrow(() -> toiletReportService.generateMonthlyReport(testUserId, start, end));

    assertThat(report).isNotNull();
    assertThat(report.size()).isEqualTo(6);
    // null이 아닌 값들만 집계됨: DEFAULT, GOLD, DARK_BROWN
    assertThat(report.colorCount()).hasSize(3); // DEFAULT(1), GOLD(1), DARK_BROWN(1)
    // null이 아닌 값들만 집계됨: BANANA, CREAM, PORRIDGE
    assertThat(report.shapeCount()).hasSize(3); // BANANA(1), CREAM(1), PORRIDGE(1)
    assertThat(report.timeDistribution()).isNotNull();
    assertThat(report.painDistribution()).isNotNull();
    assertThat(report.periodCount()).isNotEmpty();
  }

  @Test
  @DisplayName("ToiletScore가 없는 경우가 주어지는 경우 리포트 생성에 성공한다")
  void givenNoToiletScores_whenGenerateMonthlyReport_thenScoreSummaryHasNulls() {
    // Given
    LocalDateTime baseDate = LocalDateTime.of(2025, 10, 15, 10, 0);
    ActivityAt start = ActivityAt.of(LocalDate.of(2025, 10, 1));
    ActivityAt end = ActivityAt.of(LocalDate.of(2025, 11, 1));

    createToiletRecord(baseDate, true, ToiletColor.DEFAULT, ToiletShape.BANANA, 10, 5, "점수 없는 기록");

    // When
    MonthlyToiletReport report =
        assertDoesNotThrow(() -> toiletReportService.generateMonthlyReport(testUserId, start, end));

    // Then
    assertThat(report).isNotNull();
    assertThat(report.scoreSummary()).isNotNull();
    assertThat(report.scoreSummary().best()).isNull();
    assertThat(report.scoreSummary().worst()).isNull();
  }

  @Test
  @DisplayName("지난 달 데이터만 있고 현재 달 데이터가 없는 경우 size가 0이다")
  void givenOnlyLastMonthData_whenGenerateMonthlyReport_thenSizeIsZero() {
    // Given
    LocalDateTime lastMonthDate = LocalDateTime.of(2025, 9, 15, 10, 0);
    ActivityAt start = ActivityAt.of(LocalDate.of(2025, 10, 1));
    ActivityAt end = ActivityAt.of(LocalDate.of(2025, 11, 1));

    createToiletRecord(
        lastMonthDate, true, ToiletColor.DEFAULT, ToiletShape.BANANA, 10, 5, "지난 달 기록만");

    // When
    MonthlyToiletReport report =
        assertDoesNotThrow(() -> toiletReportService.generateMonthlyReport(testUserId, start, end));

    // Then
    assertThat(report).isNotNull();
    assertThat(report.size()).isZero();
  }

  @Test
  @DisplayName("1건의 ToiletRecord가 주어지는 경우에도 리포트 생성에 성공한다")
  void givenSingleToiletRecord_whenGenerateMonthlyReport_thenNoNPE() {
    // Given
    LocalDateTime baseDate = LocalDateTime.of(2025, 10, 15, 10, 0);
    ActivityAt start = ActivityAt.of(LocalDate.of(2025, 10, 1));
    ActivityAt end = ActivityAt.of(LocalDate.of(2025, 11, 1));

    createToiletRecord(baseDate, true, ToiletColor.DEFAULT, ToiletShape.BANANA, 10, 5, "단일 기록");

    // When
    MonthlyToiletReport report =
        assertDoesNotThrow(() -> toiletReportService.generateMonthlyReport(testUserId, start, end));

    // Then
    assertThat(report).isNotNull();
    assertThat(report.size()).isEqualTo(1);
    assertThat(report.shapeCount()).hasSize(1);
    assertThat(report.colorCount()).hasSize(1);
  }

  @Test
  @DisplayName("같은 color/shape가 여러 건인 경우에도 정확하게 카운트한다")
  void givenMultipleSameColorAndShape_whenGenerateMonthlyReport_thenCorrectCounts() {
    // Given
    LocalDateTime baseDate = LocalDateTime.of(2025, 10, 15, 10, 0);
    ActivityAt start = ActivityAt.of(LocalDate.of(2025, 10, 1));
    ActivityAt end = ActivityAt.of(LocalDate.of(2025, 11, 1));

    createToiletRecord(baseDate, true, ToiletColor.DEFAULT, ToiletShape.BANANA, 10, 5, "기록 1");
    createToiletRecord(
        baseDate.plusDays(1), true, ToiletColor.DEFAULT, ToiletShape.BANANA, 15, 7, "기록 2");
    createToiletRecord(
        baseDate.plusDays(2), true, ToiletColor.DEFAULT, ToiletShape.BANANA, 5, 3, "기록 3");

    // When
    MonthlyToiletReport report =
        assertDoesNotThrow(() -> toiletReportService.generateMonthlyReport(testUserId, start, end));

    // Then
    assertThat(report).isNotNull();
    assertThat(report.size()).isEqualTo(3);
    assertThat(report.shapeCount()).hasSize(1);
    assertThat(report.shapeCount().getFirst().count()).isEqualTo(3);
    assertThat(report.colorCount()).hasSize(1);
    assertThat(report.colorCount().getFirst().count()).isEqualTo(3);
  }

  @Test
  @DisplayName("소요 시간이 0인 경우에 리포트 생성에 성공한다")
  void givenToiletRecordsWithZeroDuration_whenGenerateMonthlyReport_thenNoNPE() {
    // Given
    LocalDateTime baseDate = LocalDateTime.of(2025, 10, 15, 10, 0);
    ActivityAt start = ActivityAt.of(LocalDate.of(2025, 10, 1));
    ActivityAt end = ActivityAt.of(LocalDate.of(2025, 11, 1));

    createToiletRecord(
        baseDate, true, ToiletColor.DEFAULT, ToiletShape.BANANA, 10, 0, "duration 0");

    // When & Then
    MonthlyToiletReport report =
        assertDoesNotThrow(() -> toiletReportService.generateMonthlyReport(testUserId, start, end));

    assertThat(report).isNotNull();
    assertThat(report.timeDistribution()).isNotNull();
  }

  @Test
  @DisplayName("통증이 0인 경우에 리포트 생성에 성공한다")
  void givenToiletRecordsWithZeroPain_whenGenerateMonthlyReport_thenNoNPE() {
    // Given
    LocalDateTime baseDate = LocalDateTime.of(2025, 10, 15, 10, 0);
    ActivityAt start = ActivityAt.of(LocalDate.of(2025, 10, 1));
    ActivityAt end = ActivityAt.of(LocalDate.of(2025, 11, 1));

    createToiletRecord(baseDate, true, ToiletColor.DEFAULT, ToiletShape.BANANA, 0, 5, "pain 0");

    // When & Then
    MonthlyToiletReport report =
        assertDoesNotThrow(() -> toiletReportService.generateMonthlyReport(testUserId, start, end));

    assertThat(report).isNotNull();
    assertThat(report.painDistribution()).isNotNull();
  }

  @Test
  @DisplayName("다양한 시간대의 ToiletRecord가 주어지는 - periodDistribution NPE 없음")
  void givenToiletRecordsAtVariousTimes_whenGenerateMonthlyReport_thenNoPeriodNPE() {
    // Given
    LocalDateTime baseDate = LocalDateTime.of(2025, 10, 15, 0, 0);
    ActivityAt start = ActivityAt.of(LocalDate.of(2025, 10, 1));
    ActivityAt end = ActivityAt.of(LocalDate.of(2025, 11, 1));

    createToiletRecord(
        baseDate.withHour(3), true, ToiletColor.DEFAULT, ToiletShape.BANANA, 10, 5, "새벽");
    createToiletRecord(
        baseDate.withHour(9), true, ToiletColor.GOLD, ToiletShape.CREAM, 15, 7, "아침");
    createToiletRecord(
        baseDate.withHour(14), true, ToiletColor.DARK_BROWN, ToiletShape.CORN, 5, 3, "오후");
    createToiletRecord(
        baseDate.withHour(20), true, ToiletColor.DARK_BROWN, ToiletShape.ROCK, 20, 10, "저녁");

    // When & Then
    MonthlyToiletReport report =
        assertDoesNotThrow(() -> toiletReportService.generateMonthlyReport(testUserId, start, end));

    assertThat(report).isNotNull();
    assertThat(report.periodCount()).isNotEmpty();
  }
}
