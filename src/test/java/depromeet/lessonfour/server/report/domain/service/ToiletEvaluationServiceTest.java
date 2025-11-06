package depromeet.lessonfour.server.report.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.report.domain.vo.monthly.DayPeriod;
import depromeet.lessonfour.server.report.domain.vo.monthly.ToiletPainDistribution;
import depromeet.lessonfour.server.report.domain.vo.monthly.ToiletPeriodCount;
import depromeet.lessonfour.server.report.domain.vo.monthly.ToiletTimeDistribution;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletColor;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletShape;
import depromeet.lessonfour.server.user.domain.entity.User;

class ToiletEvaluationServiceTest {

  private ToiletEvaluationService toiletEvaluationService;
  private User testUser;

  @BeforeEach
  void setUp() {
    toiletEvaluationService = new ToiletEvaluationService(null);
    testUser = User.register("test@example.com", "testuser", "password123");
  }

  private ToiletRecord createRecord(
      LocalDateTime dateTime, int pain, int duration, ToiletColor color, ToiletShape shape) {
    return ToiletRecord.register(
        testUser, true, color, shape, pain, duration, "test note", ActivityAt.from(dateTime));
  }

  @Nested
  @DisplayName("getTimeDistributions 테스트")
  class GetTimeDistributionsTest {
    @Test
    @DisplayName("소요 시간별로 정확하게 분류한다")
    void getTimeDistributions_withVariousDurations_classifiesCorrectly() {
      // Given
      LocalDateTime baseTime = LocalDateTime.of(2025, 10, 15, 10, 0);
      List<ToiletRecord> records = new ArrayList<>();

      // WITHIN_5: 5분 이하 (3개)
      records.add(createRecord(baseTime, 10, 3, ToiletColor.DEFAULT, ToiletShape.BANANA));
      records.add(createRecord(baseTime, 10, 5, ToiletColor.DEFAULT, ToiletShape.BANANA));
      records.add(createRecord(baseTime, 10, 1, ToiletColor.DEFAULT, ToiletShape.BANANA));

      // OVER_5: 5분 초과 10분 이하 (2개)
      records.add(createRecord(baseTime, 10, 6, ToiletColor.DEFAULT, ToiletShape.BANANA));
      records.add(createRecord(baseTime, 10, 10, ToiletColor.DEFAULT, ToiletShape.BANANA));

      // OVER_10: 10분 초과 (1개)
      records.add(createRecord(baseTime, 10, 15, ToiletColor.DEFAULT, ToiletShape.BANANA));

      // When
      ToiletTimeDistribution result = toiletEvaluationService.getTimeDistributions(records);

      // Then
      assertThat(result.within5min()).isEqualTo(3);
      assertThat(result.over5min()).isEqualTo(2);
      assertThat(result.over10min()).isEqualTo(1);
    }

    @Test
    @DisplayName("빈 리스트가 주어지면 모든 값이 0인 분포를 반환한다")
    void getTimeDistributions_withEmptyList_returnsZeroDistribution() {
      // Given
      List<ToiletRecord> records = new ArrayList<>();

      // When
      ToiletTimeDistribution result = toiletEvaluationService.getTimeDistributions(records);

      // Then
      assertThat(result.within5min()).isZero();
      assertThat(result.over5min()).isZero();
      assertThat(result.over10min()).isZero();
    }

    @Test
    @DisplayName("경계값 테스트: 5분과 10분 정확히")
    void getTimeDistributions_withBoundaryValues_classifiesCorrectly() {
      // Given
      LocalDateTime baseTime = LocalDateTime.of(2025, 10, 15, 10, 0);
      List<ToiletRecord> records = new ArrayList<>();

      records.add(
          createRecord(baseTime, 10, 5, ToiletColor.DEFAULT, ToiletShape.BANANA)); // 5분 -> WITHIN_5
      records.add(
          createRecord(baseTime, 10, 10, ToiletColor.DEFAULT, ToiletShape.BANANA)); // 10분 -> OVER_5
      records.add(
          createRecord(
              baseTime, 10, 11, ToiletColor.DEFAULT, ToiletShape.BANANA)); // 11분 -> OVER_10

      // When
      ToiletTimeDistribution result = toiletEvaluationService.getTimeDistributions(records);

      // Then
      assertThat(result.within5min()).isEqualTo(1);
      assertThat(result.over5min()).isEqualTo(1);
      assertThat(result.over10min()).isEqualTo(1);
    }

    @Test
    @DisplayName("0분 duration도 정상 처리한다")
    void getTimeDistributions_withZeroDuration_handlesCorrectly() {
      // Given
      LocalDateTime baseTime = LocalDateTime.of(2025, 10, 15, 10, 0);
      List<ToiletRecord> records = new ArrayList<>();

      records.add(createRecord(baseTime, 10, 0, ToiletColor.DEFAULT, ToiletShape.BANANA));

      // When
      ToiletTimeDistribution result = toiletEvaluationService.getTimeDistributions(records);

      // Then
      assertThat(result.within5min()).isEqualTo(1);
      assertThat(result.over5min()).isZero();
      assertThat(result.over10min()).isZero();
    }
  }

  @Nested
  @DisplayName("getPainDistribution 테스트")
  class GetPainDistributionsTest {
    @Test
    @DisplayName("통증 레벨별로 정확하게 분류한다")
    void getPainDistribution_withVariousPainLevels_classifiesCorrectly() {
      // Given
      LocalDateTime baseTime = LocalDateTime.of(2025, 10, 15, 10, 0);
      List<ToiletRecord> currentMonth = new ArrayList<>();
      List<ToiletRecord> lastMonth = new ArrayList<>();

      // veryLow: 0-9 (2개)
      currentMonth.add(createRecord(baseTime, 5, 5, ToiletColor.DEFAULT, ToiletShape.BANANA));
      currentMonth.add(createRecord(baseTime, 9, 5, ToiletColor.DEFAULT, ToiletShape.BANANA));

      // low: 10-29 (2개)
      currentMonth.add(createRecord(baseTime, 10, 5, ToiletColor.DEFAULT, ToiletShape.BANANA));
      currentMonth.add(createRecord(baseTime, 29, 5, ToiletColor.DEFAULT, ToiletShape.BANANA));

      // medium: 30-49 (2개)
      currentMonth.add(createRecord(baseTime, 30, 5, ToiletColor.DEFAULT, ToiletShape.BANANA));
      currentMonth.add(createRecord(baseTime, 49, 5, ToiletColor.DEFAULT, ToiletShape.BANANA));

      // high: 50-69 (2개)
      currentMonth.add(createRecord(baseTime, 50, 5, ToiletColor.DEFAULT, ToiletShape.BANANA));
      currentMonth.add(createRecord(baseTime, 69, 5, ToiletColor.DEFAULT, ToiletShape.BANANA));

      // veryHigh: 70+ (1개)
      currentMonth.add(createRecord(baseTime, 70, 5, ToiletColor.DEFAULT, ToiletShape.BANANA));

      // 지난 달: 아팠던 횟수(pain >= 50) 1개
      lastMonth.add(
          createRecord(baseTime.minusMonths(1), 60, 5, ToiletColor.DEFAULT, ToiletShape.BANANA));

      // When
      ToiletPainDistribution result =
          toiletEvaluationService.getPainDistribution(currentMonth, lastMonth);

      // Then
      assertThat(result.veryLow()).isEqualTo(2);
      assertThat(result.low()).isEqualTo(2);
      assertThat(result.medium()).isEqualTo(2);
      assertThat(result.high()).isEqualTo(2);
      assertThat(result.veryHigh()).isEqualTo(1);
      // 이번 달 아픈 횟수(3) - 지난 달 아픈 횟수(1) = 2
      assertThat(result.painDiff()).isEqualTo(2);
    }

    @Test
    @DisplayName("빈 리스트가 주어지면 모든 값이 0인 분포를 반환한다")
    void getPainDistribution_withEmptyLists_returnsZeroDistribution() {
      // Given
      List<ToiletRecord> currentMonth = new ArrayList<>();
      List<ToiletRecord> lastMonth = new ArrayList<>();

      // When
      ToiletPainDistribution result =
          toiletEvaluationService.getPainDistribution(currentMonth, lastMonth);

      // Then
      assertThat(result.veryLow()).isZero();
      assertThat(result.low()).isZero();
      assertThat(result.medium()).isZero();
      assertThat(result.high()).isZero();
      assertThat(result.veryHigh()).isZero();
      assertThat(result.painDiff()).isZero();
    }

    @Test
    @DisplayName("경계값 테스트: 각 구간의 경계값")
    void getPainDistribution_withBoundaryValues_classifiesCorrectly() {
      // Given
      LocalDateTime baseTime = LocalDateTime.of(2025, 10, 15, 10, 0);
      List<ToiletRecord> currentMonth = new ArrayList<>();
      List<ToiletRecord> lastMonth = new ArrayList<>();

      currentMonth.add(
          createRecord(baseTime, 0, 5, ToiletColor.DEFAULT, ToiletShape.BANANA)); // veryLow
      currentMonth.add(
          createRecord(baseTime, 9, 5, ToiletColor.DEFAULT, ToiletShape.BANANA)); // veryLow
      currentMonth.add(
          createRecord(baseTime, 10, 5, ToiletColor.DEFAULT, ToiletShape.BANANA)); // low
      currentMonth.add(
          createRecord(baseTime, 29, 5, ToiletColor.DEFAULT, ToiletShape.BANANA)); // low
      currentMonth.add(
          createRecord(baseTime, 30, 5, ToiletColor.DEFAULT, ToiletShape.BANANA)); // medium
      currentMonth.add(
          createRecord(baseTime, 49, 5, ToiletColor.DEFAULT, ToiletShape.BANANA)); // medium
      currentMonth.add(
          createRecord(baseTime, 50, 5, ToiletColor.DEFAULT, ToiletShape.BANANA)); // high
      currentMonth.add(
          createRecord(baseTime, 69, 5, ToiletColor.DEFAULT, ToiletShape.BANANA)); // high
      currentMonth.add(
          createRecord(baseTime, 70, 5, ToiletColor.DEFAULT, ToiletShape.BANANA)); // veryHigh
      currentMonth.add(
          createRecord(baseTime, 100, 5, ToiletColor.DEFAULT, ToiletShape.BANANA)); // veryHigh

      // When
      ToiletPainDistribution result =
          toiletEvaluationService.getPainDistribution(currentMonth, lastMonth);

      // Then
      assertThat(result.veryLow()).isEqualTo(2);
      assertThat(result.low()).isEqualTo(2);
      assertThat(result.medium()).isEqualTo(2);
      assertThat(result.high()).isEqualTo(2);
      assertThat(result.veryHigh()).isEqualTo(2);
    }

    @Test
    @DisplayName("painDiff가 음수가 될 수 있다 (이번 달이 지난 달보다 적음)")
    void getPainDistribution_withLessPainThanLastMonth_returnsNegativeDiff() {
      // Given
      LocalDateTime baseTime = LocalDateTime.of(2025, 10, 15, 10, 0);
      List<ToiletRecord> currentMonth = new ArrayList<>();
      List<ToiletRecord> lastMonth = new ArrayList<>();

      // 이번 달: 아픈 횟수 1개
      currentMonth.add(createRecord(baseTime, 50, 5, ToiletColor.DEFAULT, ToiletShape.BANANA));

      // 지난 달: 아픈 횟수 3개
      lastMonth.add(
          createRecord(baseTime.minusMonths(1), 50, 5, ToiletColor.DEFAULT, ToiletShape.BANANA));
      lastMonth.add(
          createRecord(baseTime.minusMonths(1), 60, 5, ToiletColor.DEFAULT, ToiletShape.BANANA));
      lastMonth.add(
          createRecord(baseTime.minusMonths(1), 70, 5, ToiletColor.DEFAULT, ToiletShape.BANANA));

      // When
      ToiletPainDistribution result =
          toiletEvaluationService.getPainDistribution(currentMonth, lastMonth);

      // Then
      assertThat(result.painDiff()).isEqualTo(-2); // 1 - 3 = -2
    }

    @Test
    @DisplayName("지난 달만 비어있는 경우 정상 동작한다")
    void getPainDistribution_withEmptyLastMonth_calculatesCorrectly() {
      // Given
      LocalDateTime baseTime = LocalDateTime.of(2025, 10, 15, 10, 0);
      List<ToiletRecord> currentMonth = new ArrayList<>();
      List<ToiletRecord> lastMonth = new ArrayList<>();

      currentMonth.add(createRecord(baseTime, 50, 5, ToiletColor.DEFAULT, ToiletShape.BANANA));
      currentMonth.add(createRecord(baseTime, 60, 5, ToiletColor.DEFAULT, ToiletShape.BANANA));

      // When
      ToiletPainDistribution result =
          toiletEvaluationService.getPainDistribution(currentMonth, lastMonth);

      // Then
      assertThat(result.high()).isEqualTo(2);
      assertThat(result.painDiff()).isEqualTo(2); // 2 - 0 = 2
    }
  }

  @Nested
  @DisplayName("getPeriodDistribution 테스트")
  class GetPeriodDistributionTest {
    @Test
    @DisplayName("시간대별로 정확하게 분류한다")
    void getPeriodDistribution_withVariousTimes_classifiesCorrectly() {
      // Given
      LocalDateTime baseDate = LocalDateTime.of(2025, 10, 15, 0, 0);
      List<ToiletRecord> records = new ArrayList<>();

      // MORNING: 5:00 ~ 11:59 (3개)
      records.add(
          createRecord(baseDate.withHour(5), 10, 5, ToiletColor.DEFAULT, ToiletShape.BANANA));
      records.add(
          createRecord(baseDate.withHour(8), 10, 5, ToiletColor.DEFAULT, ToiletShape.BANANA));
      records.add(
          createRecord(
              baseDate.withHour(11).withMinute(59),
              10,
              5,
              ToiletColor.DEFAULT,
              ToiletShape.BANANA));

      // AFTERNOON: 12:00 ~ 17:59 (2개)
      records.add(
          createRecord(baseDate.withHour(12), 10, 5, ToiletColor.DEFAULT, ToiletShape.BANANA));
      records.add(
          createRecord(
              baseDate.withHour(17).withMinute(59),
              10,
              5,
              ToiletColor.DEFAULT,
              ToiletShape.BANANA));

      // EVENING: 18:00 ~ 04:59 (4개)
      records.add(
          createRecord(baseDate.withHour(18), 10, 5, ToiletColor.DEFAULT, ToiletShape.BANANA));
      records.add(
          createRecord(baseDate.withHour(23), 10, 5, ToiletColor.DEFAULT, ToiletShape.BANANA));
      records.add(
          createRecord(baseDate.withHour(0), 10, 5, ToiletColor.DEFAULT, ToiletShape.BANANA));
      records.add(
          createRecord(
              baseDate.withHour(4).withMinute(59), 10, 5, ToiletColor.DEFAULT, ToiletShape.BANANA));

      // When
      List<ToiletPeriodCount> result = toiletEvaluationService.getPeriodDistribution(records);

      // Then
      assertThat(result).hasSize(3);
      assertThat(result)
          .extracting(ToiletPeriodCount::period, ToiletPeriodCount::count)
          .containsExactlyInAnyOrder(
              tuple(DayPeriod.MORNING, 3),
              tuple(DayPeriod.AFTERNOON, 2),
              tuple(DayPeriod.EVENING, 4));
    }

    @Test
    @DisplayName("빈 리스트가 주어지면 모든 period에 대해 count 0을 반환한다")
    void getPeriodDistribution_withEmptyList_returnsZeroForAllPeriods() {
      // Given
      List<ToiletRecord> records = new ArrayList<>();

      // When
      List<ToiletPeriodCount> result = toiletEvaluationService.getPeriodDistribution(records);

      // Then
      assertThat(result).hasSize(3);
      assertThat(result).allMatch(count -> count.count() == 0);
      assertThat(result)
          .extracting(ToiletPeriodCount::period)
          .containsExactlyInAnyOrder(DayPeriod.MORNING, DayPeriod.AFTERNOON, DayPeriod.EVENING);
    }

    @Test
    @DisplayName("경계값 테스트: 각 시간대의 경계 시간")
    void getPeriodDistribution_withBoundaryTimes_classifiesCorrectly() {
      // Given
      LocalDateTime baseDate = LocalDateTime.of(2025, 10, 15, 0, 0);
      List<ToiletRecord> records = new ArrayList<>();

      // 경계 시간들
      records.add(
          createRecord(
              baseDate.withHour(4).withMinute(59),
              10,
              5,
              ToiletColor.DEFAULT,
              ToiletShape.BANANA)); // EVENING
      records.add(
          createRecord(
              baseDate.withHour(5).withMinute(0),
              10,
              5,
              ToiletColor.DEFAULT,
              ToiletShape.BANANA)); // MORNING
      records.add(
          createRecord(
              baseDate.withHour(11).withMinute(59),
              10,
              5,
              ToiletColor.DEFAULT,
              ToiletShape.BANANA)); // MORNING
      records.add(
          createRecord(
              baseDate.withHour(12).withMinute(0),
              10,
              5,
              ToiletColor.DEFAULT,
              ToiletShape.BANANA)); // AFTERNOON
      records.add(
          createRecord(
              baseDate.withHour(17).withMinute(59),
              10,
              5,
              ToiletColor.DEFAULT,
              ToiletShape.BANANA)); // AFTERNOON
      records.add(
          createRecord(
              baseDate.withHour(18).withMinute(0),
              10,
              5,
              ToiletColor.DEFAULT,
              ToiletShape.BANANA)); // EVENING

      // When
      List<ToiletPeriodCount> result = toiletEvaluationService.getPeriodDistribution(records);

      // Then
      assertThat(result)
          .extracting(ToiletPeriodCount::period, ToiletPeriodCount::count)
          .containsExactlyInAnyOrder(
              tuple(DayPeriod.MORNING, 2),
              tuple(DayPeriod.AFTERNOON, 2),
              tuple(DayPeriod.EVENING, 2));
    }

    @Test
    @DisplayName("한 시간대에만 기록이 있어도 모든 period를 반환한다")
    void getPeriodDistribution_withSinglePeriod_returnsAllPeriodsWithCounts() {
      // Given
      LocalDateTime baseDate = LocalDateTime.of(2025, 10, 15, 8, 0);
      List<ToiletRecord> records = new ArrayList<>();

      records.add(createRecord(baseDate, 10, 5, ToiletColor.DEFAULT, ToiletShape.BANANA));
      records.add(createRecord(baseDate, 10, 5, ToiletColor.DEFAULT, ToiletShape.BANANA));

      // When
      List<ToiletPeriodCount> result = toiletEvaluationService.getPeriodDistribution(records);

      // Then
      assertThat(result).hasSize(3);
      assertThat(result)
          .extracting(ToiletPeriodCount::period, ToiletPeriodCount::count)
          .containsExactlyInAnyOrder(
              tuple(DayPeriod.MORNING, 2),
              tuple(DayPeriod.AFTERNOON, 0),
              tuple(DayPeriod.EVENING, 0));
    }
  }

  private Tuple tuple(Object... values) {
    return Tuple.tuple(values);
  }
}
