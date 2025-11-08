package depromeet.lessonfour.server.activityrecord.infra.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import depromeet.lessonfour.server.activityrecord.domain.vo.MealFood;
import depromeet.lessonfour.server.activityrecord.domain.vo.MealTime;
import depromeet.lessonfour.server.activityrecord.domain.vo.StressLevel;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.food.domain.entity.Food;
import depromeet.lessonfour.server.food.infra.repository.FoodRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Sql(
    scripts = "/sql/cleanup.sql",
    config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED),
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ActivityRecordQueryTest {

  @Autowired private ActivityRecordQuery activityRecordQuery;

  @Autowired private JpaActivityRecordRepository activityRecordRepository;

  @Autowired private FoodRepository foodRepository;

  private Long testUserId;
  private Long otherUserId;
  private Food testFood;

  @BeforeEach
  void setUp() {
    testUserId = 1L;
    otherUserId = 2L;

    // 테스트용 Food 데이터 생성
    testFood = Food.builder().name("사과").score(4.5).build();
    foodRepository.save(testFood);
  }

  @Test
  @DisplayName("어제 날짜로 조회하면 어제 생성된 기록만 반환한다")
  void
      given_yesterdayAndTodayRecords_when_findByActivityAtBetweenYesterday_then_returnsYesterdayRecordOnly() {
    // given
    LocalDate yesterday = LocalDate.now().minusDays(1);
    LocalDate today = LocalDate.now();

    // 어제 기록
    ActivityRecord yesterdayRecord =
        createActivityRecord(testUserId, yesterday.atTime(10, 0), StressLevel.LOW, 3);

    // 오늘 기록
    createActivityRecord(testUserId, today.atTime(10, 0), StressLevel.MEDIUM, 5);

    ActivityAt start = ActivityAt.of(yesterday);
    ActivityAt end = ActivityAt.of(today);

    // when
    List<ActivityRecord> results =
        activityRecordQuery.findByActivityAtBetween(testUserId, start, end);

    // then
    assertThat(results).hasSize(1);
    assertThat(results.getFirst().getId()).isEqualTo(yesterdayRecord.getId());
    assertThat(results.getFirst().getActivityAt().toDate()).isEqualTo(yesterday);
  }

  @Test
  @DisplayName("오늘 날짜로 조회하면 오늘 생성된 기록만 반환한다")
  void
      given_yesterdayAndTodayRecords_when_findByActivityAtBetweenToday_then_returnsTodayRecordOnly() {
    // given
    LocalDate yesterday = LocalDate.now().minusDays(1);
    LocalDate today = LocalDate.now();
    LocalDate tomorrow = LocalDate.now().plusDays(1);

    // 어제 기록
    createActivityRecord(testUserId, yesterday.atTime(10, 0), StressLevel.LOW, 3);

    // 오늘 기록
    ActivityRecord todayRecord =
        createActivityRecord(testUserId, today.atTime(14, 30), StressLevel.MEDIUM, 5);

    ActivityAt start = ActivityAt.of(today);
    ActivityAt end = ActivityAt.of(tomorrow);

    // when
    List<ActivityRecord> results =
        activityRecordQuery.findByActivityAtBetween(testUserId, start, end);

    // then
    assertThat(results).hasSize(1);
    assertThat(results.getFirst().getId()).isEqualTo(todayRecord.getId());
    assertThat(results.getFirst().getActivityAt().toDate()).isEqualTo(today);
  }

  @Test
  @DisplayName("날짜 범위 조회 시 시작일은 포함하고 종료일은 제외한다 (start <= date < end)")
  void
      given_fourDaysRecords_when_findByActivityAtBetweenWithDateRange_then_includesStartAndExcludesEnd() {
    // given
    LocalDate date1 = LocalDate.of(2024, 1, 10);
    LocalDate date2 = LocalDate.of(2024, 1, 11);
    LocalDate date3 = LocalDate.of(2024, 1, 12);
    LocalDate date4 = LocalDate.of(2024, 1, 13);

    ActivityRecord record1 =
        createActivityRecord(testUserId, date1.atTime(10, 0), StressLevel.LOW, 3);
    ActivityRecord record2 =
        createActivityRecord(testUserId, date2.atTime(10, 0), StressLevel.LOW, 4);
    ActivityRecord record3 =
        createActivityRecord(testUserId, date3.atTime(10, 0), StressLevel.LOW, 5);
    createActivityRecord(testUserId, date4.atTime(10, 0), StressLevel.LOW, 6);

    ActivityAt start = ActivityAt.of(date1);
    ActivityAt end = ActivityAt.of(date4);

    // when
    List<ActivityRecord> results =
        activityRecordQuery.findByActivityAtBetween(testUserId, start, end);

    // then
    assertThat(results).hasSize(3);
    assertThat(results)
        .extracting(ActivityRecord::getId)
        .containsExactly(record1.getId(), record2.getId(), record3.getId());
    assertThat(results)
        .extracting(record -> record.getActivityAt().toDate())
        .containsExactly(date1, date2, date3);
  }

  @Test
  @DisplayName("삭제된 기록은 조회되지 않는다")
  void given_normalAndDeletedRecords_when_findByActivityAtBetween_then_returnsOnlyNormalRecord() {
    // given
    LocalDate today = LocalDate.now();
    LocalDate tomorrow = LocalDate.now().plusDays(1);

    // 정상 기록
    ActivityRecord normalRecord =
        createActivityRecord(testUserId, today.atTime(10, 0), StressLevel.LOW, 3);

    // 삭제된 기록
    ActivityRecord deletedRecord =
        createActivityRecord(testUserId, today.atTime(14, 0), StressLevel.MEDIUM, 5);
    deletedRecord.delete();
    activityRecordRepository.save(deletedRecord);

    ActivityAt start = ActivityAt.of(today);
    ActivityAt end = ActivityAt.of(tomorrow);

    // when
    List<ActivityRecord> results =
        activityRecordQuery.findByActivityAtBetween(testUserId, start, end);

    // then
    assertThat(results).hasSize(1);
    assertThat(results.getFirst().getId()).isEqualTo(normalRecord.getId());
    assertThat(results.getFirst().isDeleted()).isFalse();
  }

  @Test
  @DisplayName("다른 사용자의 기록은 조회되지 않는다")
  void given_myRecordAndOtherUserRecord_when_findByActivityAtBetween_then_returnsOnlyMyRecord() {
    // given
    LocalDate today = LocalDate.now();
    LocalDate tomorrow = LocalDate.now().plusDays(1);

    // 현재 사용자의 기록
    ActivityRecord myRecord =
        createActivityRecord(testUserId, today.atTime(10, 0), StressLevel.LOW, 3);

    // 다른 사용자의 기록
    createActivityRecord(otherUserId, today.atTime(14, 0), StressLevel.MEDIUM, 5);

    ActivityAt start = ActivityAt.of(today);
    ActivityAt end = ActivityAt.of(tomorrow);

    // when
    List<ActivityRecord> results =
        activityRecordQuery.findByActivityAtBetween(testUserId, start, end);

    // then
    assertThat(results).hasSize(1);
    assertThat(results.getFirst().getId()).isEqualTo(myRecord.getId());
    assertThat(results.getFirst().getUserId()).isEqualTo(testUserId);
  }

  @Test
  @DisplayName("같은 날짜에 여러 기록이 있으면 날짜 오름차순으로 정렬하여 반환한다")
  void
      given_multipleRecordsInRandomOrder_when_findByActivityAtBetween_then_returnsSortedByDateAscending() {
    // given
    LocalDate date1 = LocalDate.of(2024, 1, 10);
    LocalDate date2 = LocalDate.of(2024, 1, 11);
    LocalDate date3 = LocalDate.of(2024, 1, 12);
    LocalDate date4 = LocalDate.of(2024, 1, 13);

    // 순서를 섞어서 생성
    createActivityRecord(testUserId, date2.atTime(10, 0), StressLevel.LOW, 4);
    createActivityRecord(testUserId, date1.atTime(10, 0), StressLevel.LOW, 3);
    createActivityRecord(testUserId, date3.atTime(10, 0), StressLevel.LOW, 5);

    ActivityAt start = ActivityAt.of(date1);
    ActivityAt end = ActivityAt.of(date4);

    // when
    List<ActivityRecord> results =
        activityRecordQuery.findByActivityAtBetween(testUserId, start, end);

    // then
    assertThat(results).hasSize(3);
    assertThat(results)
        .extracting(record -> record.getActivityAt().toDate())
        .containsExactly(date1, date2, date3);
  }

  @Test
  @DisplayName("음식 기록이 있는 경우 fetch join으로 함께 조회된다")
  void
      given_recordWithFoodRecords_when_findByActivityAtBetween_then_returnsFetchJoinedFoodRecords() {
    // given
    LocalDate today = LocalDate.now();
    LocalDate tomorrow = LocalDate.now().plusDays(1);

    List<MealFood> mealFoods = List.of(new MealFood(MealTime.BREAKFAST, testFood));

    ActivityRecord recordWithFood =
        activityRecordRepository.save(
            ActivityRecord.createWithMeals(
                testUserId,
                5,
                StressLevel.MEDIUM,
                ActivityAt.from(today.atTime(10, 0)),
                mealFoods));

    ActivityAt start = ActivityAt.of(today);
    ActivityAt end = ActivityAt.of(tomorrow);

    // when
    List<ActivityRecord> results =
        activityRecordQuery.findByActivityAtBetween(testUserId, start, end);

    // then
    assertThat(results).hasSize(1);
    assertThat(results.getFirst().getId()).isEqualTo(recordWithFood.getId());
    assertThat(results.getFirst().getFoodRecords()).hasSize(1);
    assertThat(results.getFirst().getFoodRecords().getFirst().getFood().getName()).isEqualTo("사과");
  }

  @Test
  @DisplayName("조건에 맞는 기록이 없으면 빈 리스트를 반환한다")
  void given_recordInPast_when_findByActivityAtBetweenInFuture_then_returnsEmptyList() {
    // given
    LocalDate futureDate = LocalDate.now().plusDays(10);
    LocalDate futureEndDate = LocalDate.now().plusDays(11);

    createActivityRecord(testUserId, LocalDate.now().atTime(10, 0), StressLevel.LOW, 3);

    ActivityAt start = ActivityAt.of(futureDate);
    ActivityAt end = ActivityAt.of(futureEndDate);

    // when
    List<ActivityRecord> results =
        activityRecordQuery.findByActivityAtBetween(testUserId, start, end);

    // then
    assertThat(results).isEmpty();
  }

  @Test
  @DisplayName("어제와 오늘 기록이 모두 있는 경우 날짜 범위에 따라 정확히 필터링된다")
  void
      given_yesterdayAndTodayRecords_when_findByActivityAtBetweenWithVariousRanges_then_filtersCorrectly() {
    // given
    LocalDate yesterday = LocalDate.now().minusDays(1);
    LocalDate today = LocalDate.now();
    LocalDate tomorrow = LocalDate.now().plusDays(1);

    ActivityRecord yesterdayRecord =
        createActivityRecord(testUserId, yesterday.atTime(10, 0), StressLevel.LOW, 3);
    ActivityRecord todayRecord =
        createActivityRecord(testUserId, today.atTime(14, 0), StressLevel.MEDIUM, 5);

    // when - 어제만 조회
    ActivityAt startYesterday = ActivityAt.of(yesterday);
    ActivityAt endYesterday = ActivityAt.of(today);
    List<ActivityRecord> yesterdayResults =
        activityRecordQuery.findByActivityAtBetween(testUserId, startYesterday, endYesterday);

    // then
    assertThat(yesterdayResults).hasSize(1);
    assertThat(yesterdayResults.getFirst().getId()).isEqualTo(yesterdayRecord.getId());

    // when - 오늘만 조회
    ActivityAt startToday = ActivityAt.of(today);
    ActivityAt endToday = ActivityAt.of(tomorrow);
    List<ActivityRecord> todayResults =
        activityRecordQuery.findByActivityAtBetween(testUserId, startToday, endToday);

    // then
    assertThat(todayResults).hasSize(1);
    assertThat(todayResults.getFirst().getId()).isEqualTo(todayRecord.getId());

    // when - 어제부터 내일까지 조회 (어제, 오늘 모두 포함)
    ActivityAt startBoth = ActivityAt.of(yesterday);
    ActivityAt endBoth = ActivityAt.of(tomorrow);
    List<ActivityRecord> bothResults =
        activityRecordQuery.findByActivityAtBetween(testUserId, startBoth, endBoth);

    // then
    assertThat(bothResults).hasSize(2);
    assertThat(bothResults)
        .extracting(ActivityRecord::getId)
        .containsExactly(yesterdayRecord.getId(), todayRecord.getId());
  }

  private ActivityRecord createActivityRecord(
      Long userId, LocalDateTime dateTime, StressLevel stressLevel, int waterIntakeCups) {
    return activityRecordRepository.save(
        ActivityRecord.createWithMeals(
            userId, waterIntakeCups, stressLevel, ActivityAt.from(dateTime), List.of()));
  }
}
