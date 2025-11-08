package depromeet.lessonfour.server.toiletrecord.infra.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletColor;
import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletShape;
import depromeet.lessonfour.server.user.domain.entity.User;
import depromeet.lessonfour.server.user.domain.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Sql(
    scripts = "/sql/cleanup.sql",
    config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED),
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ToiletRecordQueryTest {

  @Autowired private ToiletRecordQuery toiletRecordQuery;

  @Autowired private JpaToiletRecordRepository jpaToiletRecordRepository;

  @Autowired private UserRepository userRepository;

  private User testUser;
  private User otherUser;

  @BeforeEach
  void setUp() {
    testUser = userRepository.save(User.register("test@example.com", "testuser", "password123"));
    otherUser = userRepository.save(User.register("other@example.com", "otheruser", "password456"));
  }

  @Test
  @DisplayName("같은 날짜에 여러 시간대의 기록이 있으면 시간 오름차순으로 정렬하여 반환한다")
  void
      given_multipleRecordsInSameDate_when_findAllByActivityAt_then_returnsSortedByTimeAscending() {
    // given
    LocalDate today = LocalDate.of(2024, 1, 15);

    // 시간대를 섞어서 생성 (15:00, 09:00, 12:00)
    ToiletRecord afternoon =
        createToiletRecord(testUser, today.atTime(15, 0), ToiletColor.DEFAULT, ToiletShape.BANANA);
    ToiletRecord morning =
        createToiletRecord(testUser, today.atTime(9, 0), ToiletColor.GOLD, ToiletShape.CREAM);
    ToiletRecord noon =
        createToiletRecord(testUser, today.atTime(12, 0), ToiletColor.DARK_BROWN, ToiletShape.CORN);

    ActivityAt activityAt = ActivityAt.of(today);

    // when
    List<ToiletRecord> results =
        toiletRecordQuery.findAllByActivityAt(testUser.getId(), activityAt);

    // then
    assertThat(results).hasSize(3);
    assertThat(results)
        .extracting(ToiletRecord::getId)
        .containsExactly(morning.getId(), noon.getId(), afternoon.getId());
    assertThat(results)
        .extracting(record -> record.getActivityAt().toDateTime().toLocalTime())
        .containsExactly(LocalTime.of(9, 0), LocalTime.of(12, 0), LocalTime.of(15, 0));
  }

  @Test
  @DisplayName("시간이 같은 경우에도 정렬이 안정적으로 동작한다")
  void given_recordsWithSameTime_when_findAllByActivityAt_then_returnsConsistentOrder() {
    // given
    LocalDate today = LocalDate.of(2024, 1, 15);
    LocalTime sameTime = LocalTime.of(10, 0);

    // 같은 시간에 여러 기록 생성
    ToiletRecord record1 =
        createToiletRecord(
            testUser, today.atTime(sameTime), ToiletColor.DEFAULT, ToiletShape.BANANA);
    ToiletRecord record2 =
        createToiletRecord(testUser, today.atTime(sameTime), ToiletColor.GOLD, ToiletShape.CREAM);
    ToiletRecord record3 =
        createToiletRecord(
            testUser, today.atTime(sameTime), ToiletColor.DARK_BROWN, ToiletShape.CORN);

    ActivityAt activityAt = ActivityAt.of(today);

    // when
    List<ToiletRecord> results =
        toiletRecordQuery.findAllByActivityAt(testUser.getId(), activityAt);

    // then - 모든 기록이 반환되어야 함
    assertThat(results).hasSize(3);
    assertThat(results)
        .extracting(ToiletRecord::getId)
        .containsExactlyInAnyOrder(record1.getId(), record2.getId(), record3.getId());
    assertThat(results)
        .extracting(record -> record.getActivityAt().toDateTime().toLocalTime())
        .containsOnly(sameTime);
  }

  @Test
  @DisplayName("특정 날짜의 기록만 조회하고 다른 날짜의 기록은 제외한다")
  void given_recordsInDifferentDates_when_findAllByActivityAt_then_returnsOnlySpecificDate() {
    // given
    LocalDate yesterday = LocalDate.of(2024, 1, 14);
    LocalDate today = LocalDate.of(2024, 1, 15);
    LocalDate tomorrow = LocalDate.of(2024, 1, 16);

    // 어제, 오늘, 내일 기록 생성
    createToiletRecord(testUser, yesterday.atTime(10, 0), ToiletColor.DEFAULT, ToiletShape.BANANA);
    ToiletRecord todayRecord =
        createToiletRecord(testUser, today.atTime(10, 0), ToiletColor.GOLD, ToiletShape.CREAM);
    createToiletRecord(testUser, tomorrow.atTime(10, 0), ToiletColor.DARK_BROWN, ToiletShape.CORN);

    ActivityAt activityAt = ActivityAt.of(today);

    // when
    List<ToiletRecord> results =
        toiletRecordQuery.findAllByActivityAt(testUser.getId(), activityAt);

    // then - 오늘 날짜의 기록만 반환
    assertThat(results).hasSize(1);
    assertThat(results.getFirst().getId()).isEqualTo(todayRecord.getId());
    assertThat(results.getFirst().getActivityAt().toDate()).isEqualTo(today);
  }

  @Test
  @DisplayName("삭제된 기록은 조회되지 않는다")
  void given_normalAndDeletedRecords_when_findAllByActivityAt_then_returnsOnlyNormalRecord() {
    // given
    LocalDate today = LocalDate.of(2024, 1, 15);

    // 정상 기록
    ToiletRecord normalRecord =
        createToiletRecord(testUser, today.atTime(10, 0), ToiletColor.DEFAULT, ToiletShape.BANANA);

    // 삭제된 기록
    ToiletRecord deletedRecord =
        createToiletRecord(testUser, today.atTime(14, 0), ToiletColor.GOLD, ToiletShape.CREAM);
    deletedRecord.delete();
    jpaToiletRecordRepository.save(deletedRecord);

    ActivityAt activityAt = ActivityAt.of(today);

    // when
    List<ToiletRecord> results =
        toiletRecordQuery.findAllByActivityAt(testUser.getId(), activityAt);

    // then
    assertThat(results).hasSize(1);
    assertThat(results.getFirst().getId()).isEqualTo(normalRecord.getId());
    assertThat(results.getFirst().isDeleted()).isFalse();
  }

  @Test
  @DisplayName("다른 사용자의 기록은 조회되지 않는다")
  void given_myRecordAndOtherUserRecord_when_findAllByActivityAt_then_returnsOnlyMyRecord() {
    // given
    LocalDate today = LocalDate.of(2024, 1, 15);

    // 현재 사용자의 기록
    ToiletRecord myRecord =
        createToiletRecord(testUser, today.atTime(10, 0), ToiletColor.DEFAULT, ToiletShape.BANANA);

    // 다른 사용자의 기록
    createToiletRecord(otherUser, today.atTime(14, 0), ToiletColor.GOLD, ToiletShape.CREAM);

    ActivityAt activityAt = ActivityAt.of(today);

    // when
    List<ToiletRecord> results =
        toiletRecordQuery.findAllByActivityAt(testUser.getId(), activityAt);

    // then
    assertThat(results).hasSize(1);
    assertThat(results.getFirst().getId()).isEqualTo(myRecord.getId());
    assertThat(results.getFirst().getUser().getId()).isEqualTo(testUser.getId());
  }

  @Test
  @DisplayName("조건에 맞는 기록이 없으면 빈 리스트를 반환한다")
  void given_recordInOtherDate_when_findAllByActivityAt_then_returnsEmptyList() {
    // given
    LocalDate today = LocalDate.of(2024, 1, 15);
    LocalDate otherDate = LocalDate.of(2024, 1, 20);

    // 다른 날짜에 기록 생성
    createToiletRecord(testUser, otherDate.atTime(10, 0), ToiletColor.DEFAULT, ToiletShape.BANANA);

    ActivityAt activityAt = ActivityAt.of(today);

    // when
    List<ToiletRecord> results =
        toiletRecordQuery.findAllByActivityAt(testUser.getId(), activityAt);

    // then
    assertThat(results).isEmpty();
  }

  @Test
  @DisplayName("아침, 점심, 저녁 시간대의 기록이 시간 순서대로 정렬된다")
  void given_morningLunchDinnerRecords_when_findAllByActivityAt_then_returnsSortedByTime() {
    // given
    LocalDate today = LocalDate.of(2024, 1, 15);

    // 역순으로 생성 (저녁, 점심, 아침)
    ToiletRecord dinner =
        createToiletRecord(testUser, today.atTime(19, 30), ToiletColor.DEFAULT, ToiletShape.BANANA);
    ToiletRecord lunch =
        createToiletRecord(testUser, today.atTime(13, 0), ToiletColor.GOLD, ToiletShape.CREAM);
    ToiletRecord morning =
        createToiletRecord(testUser, today.atTime(8, 15), ToiletColor.DARK_BROWN, ToiletShape.CORN);

    ActivityAt activityAt = ActivityAt.of(today);

    // when
    List<ToiletRecord> results =
        toiletRecordQuery.findAllByActivityAt(testUser.getId(), activityAt);

    // then
    assertThat(results).hasSize(3);
    assertThat(results)
        .extracting(ToiletRecord::getId)
        .containsExactly(morning.getId(), lunch.getId(), dinner.getId());
    assertThat(results)
        .extracting(record -> record.getActivityAt().toDateTime().toLocalTime())
        .containsExactly(LocalTime.of(8, 15), LocalTime.of(13, 0), LocalTime.of(19, 30));
  }

  @Test
  @DisplayName("자정 근처의 시간대도 정확하게 정렬된다")
  void given_midnightRecords_when_findAllByActivityAt_then_returnsSortedCorrectly() {
    // given
    LocalDate today = LocalDate.of(2024, 1, 15);

    // 자정 근처 시간대 (23:50, 00:10, 00:00, 23:59)
    ToiletRecord latNight =
        createToiletRecord(testUser, today.atTime(23, 50), ToiletColor.DEFAULT, ToiletShape.BANANA);
    ToiletRecord afterMidnight =
        createToiletRecord(testUser, today.atTime(0, 10), ToiletColor.GOLD, ToiletShape.CREAM);
    ToiletRecord midnight =
        createToiletRecord(testUser, today.atTime(0, 0), ToiletColor.DARK_BROWN, ToiletShape.CORN);
    ToiletRecord almostMidnight =
        createToiletRecord(testUser, today.atTime(23, 59), ToiletColor.RED, ToiletShape.ROCK);

    ActivityAt activityAt = ActivityAt.of(today);

    // when
    List<ToiletRecord> results =
        toiletRecordQuery.findAllByActivityAt(testUser.getId(), activityAt);

    // then
    assertThat(results).hasSize(4);
    assertThat(results)
        .extracting(ToiletRecord::getId)
        .containsExactly(
            midnight.getId(), afterMidnight.getId(), latNight.getId(), almostMidnight.getId());
    assertThat(results)
        .extracting(record -> record.getActivityAt().toDateTime().toLocalTime())
        .containsExactly(
            LocalTime.of(0, 0), LocalTime.of(0, 10), LocalTime.of(23, 50), LocalTime.of(23, 59));
  }

  private ToiletRecord createToiletRecord(
      User user, LocalDateTime dateTime, ToiletColor color, ToiletShape shape) {
    return jpaToiletRecordRepository.save(
        ToiletRecord.register(
            user, true, color, shape, 50, 5, "test note", ActivityAt.from(dateTime)));
  }
}
