package depromeet.lessonfour.server.report.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import depromeet.lessonfour.server.activityrecord.domain.vo.StressLevel;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.report.app.client.ActivityRecordClient;
import depromeet.lessonfour.server.report.domain.vo.activity.DailyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.activity.StressEvaluation;

@ExtendWith(MockitoExtension.class)
class ActivityReportServiceTest {

  @Mock private ActivityRecordClient activityRecordClient;

  @InjectMocks private ActivityReportService activityReportService;

  private Long testUserId;
  private ActivityAt testActivityAt;
  private ActivityRecord yesterdayRecord;
  private ActivityRecord todayRecord;

  @BeforeEach
  void setUp() {
    testUserId = 1L;
    testActivityAt = ActivityAt.of(LocalDate.of(2025, 1, 9));

    // Create test records with different dates
    yesterdayRecord =
        ActivityRecord.createWithMeals(
            testUserId,
            5,
            StressLevel.MEDIUM,
            ActivityAt.of(LocalDate.of(2025, 1, 8)), // yesterday
            null);

    todayRecord =
        ActivityRecord.createWithMeals(
            testUserId,
            7,
            StressLevel.LOW,
            ActivityAt.of(LocalDate.of(2025, 1, 9)), // today
            null);
  }

  @Test
  @DisplayName("generateDailyReport - 어제와 오늘 기록이 모두 있으면 날짜별로 정확하게 구분하여 처리한다")
  void generateDailyReport_withBothYesterdayAndTodayRecords_distinguishesByDate() {
    // Given
    List<ActivityRecord> records = List.of(yesterdayRecord, todayRecord);

    when(activityRecordClient.getActivityRecordsBetween(
            eq(testUserId), any(ActivityAt.class), any(ActivityAt.class)))
        .thenReturn(records);

    DailyActivityReport mockReport =
        new DailyActivityReport(new ArrayList<>(), new ArrayList<>(), StressEvaluation.LOW);

    try (MockedStatic<DailyActivityReport> mockedStatic = mockStatic(DailyActivityReport.class)) {
      mockedStatic
          .when(
              () ->
                  DailyActivityReport.evaluate(
                      any(ActivityRecord.class), any(ActivityRecord.class)))
          .thenReturn(mockReport);

      // When
      DailyActivityReport result =
          activityReportService.generateDailyReport(testUserId, testActivityAt);

      // Then
      ArgumentCaptor<ActivityAt> startAtCaptor = ArgumentCaptor.forClass(ActivityAt.class);
      ArgumentCaptor<ActivityAt> endAtCaptor = ArgumentCaptor.forClass(ActivityAt.class);

      // Verify client was called with correct date range
      verify(activityRecordClient)
          .getActivityRecordsBetween(
              eq(testUserId), startAtCaptor.capture(), endAtCaptor.capture());

      assertThat(startAtCaptor.getValue().toDate())
          .isEqualTo(LocalDate.of(2025, 1, 8)); // dayBefore
      assertThat(endAtCaptor.getValue().toDate()).isEqualTo(LocalDate.of(2025, 1, 10)); // dayAfter

      // Verify evaluation service was called with correct records by date
      ArgumentCaptor<ActivityRecord> previousCaptor = ArgumentCaptor.forClass(ActivityRecord.class);
      ArgumentCaptor<ActivityRecord> currentCaptor = ArgumentCaptor.forClass(ActivityRecord.class);

      mockedStatic.verify(
          () -> DailyActivityReport.evaluate(previousCaptor.capture(), currentCaptor.capture()));

      assertThat(previousCaptor.getValue()).isEqualTo(yesterdayRecord);
      assertThat(currentCaptor.getValue()).isEqualTo(todayRecord);

      assertThat(result).isEqualTo(mockReport);
    }
  }

  @Test
  @DisplayName("generateDailyReport - 레코드가 날짜 역순으로 반환되어도 날짜로 정확하게 구분한다")
  void generateDailyReport_withReverseOrderRecords_stillDistinguishesByDate() {
    // Given: Records in reverse order (today first, yesterday last)
    List<ActivityRecord> recordsInReverseOrder = List.of(todayRecord, yesterdayRecord);

    when(activityRecordClient.getActivityRecordsBetween(
            eq(testUserId), any(ActivityAt.class), any(ActivityAt.class)))
        .thenReturn(recordsInReverseOrder);

    DailyActivityReport mockReport =
        new DailyActivityReport(new ArrayList<>(), new ArrayList<>(), StressEvaluation.LOW);

    try (MockedStatic<DailyActivityReport> mockedStatic = mockStatic(DailyActivityReport.class)) {
      mockedStatic
          .when(
              () ->
                  DailyActivityReport.evaluate(
                      any(ActivityRecord.class), any(ActivityRecord.class)))
          .thenReturn(mockReport);

      // When
      DailyActivityReport result =
          activityReportService.generateDailyReport(testUserId, testActivityAt);

      // Then
      ArgumentCaptor<ActivityRecord> previousCaptor = ArgumentCaptor.forClass(ActivityRecord.class);
      ArgumentCaptor<ActivityRecord> currentCaptor = ArgumentCaptor.forClass(ActivityRecord.class);

      mockedStatic.verify(
          () -> DailyActivityReport.evaluate(previousCaptor.capture(), currentCaptor.capture()));

      // Should still correctly identify by date, not by list position
      assertThat(previousCaptor.getValue()).isEqualTo(yesterdayRecord);
      assertThat(currentCaptor.getValue()).isEqualTo(todayRecord);

      assertThat(result).isEqualTo(mockReport);
    }
  }

  @Test
  @DisplayName("generateDailyReport - 정확한 날짜 범위(dayBefore ~ dayAfter)로 클라이언트를 호출한다")
  void generateDailyReport_callsClientWithCorrectDateRange() {
    // Given
    ActivityAt baseDate = ActivityAt.of(LocalDate.of(2025, 1, 15));

    // Create records matching the base date
    ActivityRecord jan14Record =
        ActivityRecord.createWithMeals(
            testUserId, 5, StressLevel.MEDIUM, ActivityAt.of(LocalDate.of(2025, 1, 14)), null);
    ActivityRecord jan15Record =
        ActivityRecord.createWithMeals(
            testUserId, 7, StressLevel.LOW, ActivityAt.of(LocalDate.of(2025, 1, 15)), null);

    List<ActivityRecord> records = List.of(jan14Record, jan15Record);

    when(activityRecordClient.getActivityRecordsBetween(
            eq(testUserId), any(ActivityAt.class), any(ActivityAt.class)))
        .thenReturn(records);

    DailyActivityReport mockReport =
        new DailyActivityReport(new ArrayList<>(), new ArrayList<>(), StressEvaluation.LOW);

    try (MockedStatic<DailyActivityReport> mockedStatic = mockStatic(DailyActivityReport.class)) {
      mockedStatic.when(() -> DailyActivityReport.evaluate(any(), any())).thenReturn(mockReport);

      // When
      activityReportService.generateDailyReport(testUserId, baseDate);

      // Then
      ArgumentCaptor<ActivityAt> startAtCaptor = ArgumentCaptor.forClass(ActivityAt.class);
      ArgumentCaptor<ActivityAt> endAtCaptor = ArgumentCaptor.forClass(ActivityAt.class);

      verify(activityRecordClient)
          .getActivityRecordsBetween(
              eq(testUserId), startAtCaptor.capture(), endAtCaptor.capture());

      // Verify date range is from dayBefore (Jan 14) to dayAfter (Jan 16)
      assertThat(startAtCaptor.getValue().toDate()).isEqualTo(LocalDate.of(2025, 1, 14));
      assertThat(endAtCaptor.getValue().toDate()).isEqualTo(LocalDate.of(2025, 1, 16));
    }
  }

  @Test
  @DisplayName("generateDailyReport - 어제 기록만 존재하는 경우 yesterday만 전달하고 today는 null")
  void generateDailyReport_withOnlyYesterdayRecord_passesYesterdayAndNullToday() {
    // Given: Only yesterday's record exists
    List<ActivityRecord> records = List.of(yesterdayRecord);

    when(activityRecordClient.getActivityRecordsBetween(
            eq(testUserId), any(ActivityAt.class), any(ActivityAt.class)))
        .thenReturn(records);

    DailyActivityReport mockReport =
        new DailyActivityReport(new ArrayList<>(), new ArrayList<>(), StressEvaluation.LOW);

    try (MockedStatic<DailyActivityReport> mockedStatic = mockStatic(DailyActivityReport.class)) {
      mockedStatic.when(() -> DailyActivityReport.evaluate(any(), any())).thenReturn(mockReport);

      // When
      activityReportService.generateDailyReport(testUserId, testActivityAt);

      // Then
      ArgumentCaptor<ActivityRecord> previousCaptor = ArgumentCaptor.forClass(ActivityRecord.class);
      ArgumentCaptor<ActivityRecord> currentCaptor = ArgumentCaptor.forClass(ActivityRecord.class);

      mockedStatic.verify(
          () -> DailyActivityReport.evaluate(previousCaptor.capture(), currentCaptor.capture()));

      assertThat(previousCaptor.getValue()).isEqualTo(yesterdayRecord);
      assertThat(currentCaptor.getValue()).isNull();
    }
  }

  @Test
  @DisplayName("generateDailyReport - 오늘 기록만 존재하는 경우 yesterday는 null이고 today만 전달")
  void generateDailyReport_withOnlyTodayRecord_passesNullYesterdayAndToday() {
    // Given: Only today's record exists
    List<ActivityRecord> records = List.of(todayRecord);

    when(activityRecordClient.getActivityRecordsBetween(
            eq(testUserId), any(ActivityAt.class), any(ActivityAt.class)))
        .thenReturn(records);

    DailyActivityReport mockReport =
        new DailyActivityReport(new ArrayList<>(), new ArrayList<>(), StressEvaluation.LOW);

    try (MockedStatic<DailyActivityReport> mockedStatic = mockStatic(DailyActivityReport.class)) {
      mockedStatic.when(() -> DailyActivityReport.evaluate(any(), any())).thenReturn(mockReport);

      // When
      activityReportService.generateDailyReport(testUserId, testActivityAt);

      // Then
      ArgumentCaptor<ActivityRecord> previousCaptor = ArgumentCaptor.forClass(ActivityRecord.class);
      ArgumentCaptor<ActivityRecord> currentCaptor = ArgumentCaptor.forClass(ActivityRecord.class);

      mockedStatic.verify(
          () -> DailyActivityReport.evaluate(previousCaptor.capture(), currentCaptor.capture()));

      assertThat(previousCaptor.getValue()).isNull();
      assertThat(currentCaptor.getValue()).isEqualTo(todayRecord);
    }
  }

  @Test
  @DisplayName("generateDailyReport - 레코드가 없는 경우 yesterday와 today 모두 null로 전달")
  void generateDailyReport_withNoRecords_passesBothNull() {
    // Given: No records
    List<ActivityRecord> records = List.of();

    when(activityRecordClient.getActivityRecordsBetween(
            eq(testUserId), any(ActivityAt.class), any(ActivityAt.class)))
        .thenReturn(records);

    DailyActivityReport mockReport =
        new DailyActivityReport(new ArrayList<>(), new ArrayList<>(), StressEvaluation.LOW);

    try (MockedStatic<DailyActivityReport> mockedStatic = mockStatic(DailyActivityReport.class)) {
      mockedStatic.when(() -> DailyActivityReport.evaluate(any(), any())).thenReturn(mockReport);

      // When
      activityReportService.generateDailyReport(testUserId, testActivityAt);

      // Then
      ArgumentCaptor<ActivityRecord> previousCaptor = ArgumentCaptor.forClass(ActivityRecord.class);
      ArgumentCaptor<ActivityRecord> currentCaptor = ArgumentCaptor.forClass(ActivityRecord.class);

      mockedStatic.verify(
          () -> DailyActivityReport.evaluate(previousCaptor.capture(), currentCaptor.capture()));

      assertThat(previousCaptor.getValue()).isNull();
      assertThat(currentCaptor.getValue()).isNull();
    }
  }

  @Test
  @DisplayName("generateDailyReport - 어제도 오늘도 아닌 날짜의 레코드는 무시하고 둘 다 null로 전달")
  void generateDailyReport_withIrrelevantDateRecords_passesBothNull() {
    // Given: Record from a different date (not yesterday or today)
    ActivityRecord irrelevantRecord =
        ActivityRecord.createWithMeals(
            testUserId,
            5,
            StressLevel.MEDIUM,
            ActivityAt.of(LocalDate.of(2025, 1, 10)), // dayAfter, neither yesterday nor today
            null);

    List<ActivityRecord> records = List.of(irrelevantRecord);

    when(activityRecordClient.getActivityRecordsBetween(
            eq(testUserId), any(ActivityAt.class), any(ActivityAt.class)))
        .thenReturn(records);

    DailyActivityReport mockReport =
        new DailyActivityReport(new ArrayList<>(), new ArrayList<>(), StressEvaluation.LOW);

    try (MockedStatic<DailyActivityReport> mockedStatic = mockStatic(DailyActivityReport.class)) {
      mockedStatic.when(() -> DailyActivityReport.evaluate(any(), any())).thenReturn(mockReport);

      // When
      activityReportService.generateDailyReport(testUserId, testActivityAt);

      // Then
      ArgumentCaptor<ActivityRecord> previousCaptor = ArgumentCaptor.forClass(ActivityRecord.class);
      ArgumentCaptor<ActivityRecord> currentCaptor = ArgumentCaptor.forClass(ActivityRecord.class);

      mockedStatic.verify(
          () -> DailyActivityReport.evaluate(previousCaptor.capture(), currentCaptor.capture()));

      assertThat(previousCaptor.getValue()).isNull();
      assertThat(currentCaptor.getValue()).isNull();
    }
  }
}
