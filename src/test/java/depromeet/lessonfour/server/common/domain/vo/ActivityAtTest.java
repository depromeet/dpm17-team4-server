package depromeet.lessonfour.server.common.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import depromeet.lessonfour.server.common.api.code.ErrorCode;
import depromeet.lessonfour.server.common.exception.ServerException;

class ActivityAtTest {

  @Nested
  @DisplayName("of() 팩토리 메서드")
  class OfFactoryMethod {

    @Test
    @DisplayName("null 날짜로 생성하면 EMPTY를 반환한다")
    void givenNullDate_whenOf_thenReturnsEmpty() {
      // given
      LocalDate nullDate = null;

      // when
      ActivityAt result = ActivityAt.of(nullDate);

      // then
      assertThat(result).isNotNull();
      assertThat(result.toDate()).isEqualTo(LocalDate.MIN);
      assertThat(result.toDateTime().toLocalTime()).isEqualTo(LocalTime.MIN);
    }

    @Test
    @DisplayName("정상 날짜로 생성하면 자정 시간으로 설정된다")
    void givenValidDate_whenOf_thenReturnsActivityAtWithMidnight() {
      // given
      LocalDate date = LocalDate.of(2024, 1, 15);

      // when
      ActivityAt result = ActivityAt.of(date);

      // then
      assertThat(result.toDate()).isEqualTo(date);
      assertThat(result.toDateTime().toLocalTime()).isEqualTo(LocalTime.MIDNIGHT);
    }
  }

  @Nested
  @DisplayName("from() 팩토리 메서드")
  class FromFactoryMethod {

    @Test
    @DisplayName("null LocalDateTime으로 생성하면 EMPTY를 반환한다")
    void givenNullDateTime_whenFrom_thenReturnsEmpty() {
      // given
      LocalDateTime nullDateTime = null;

      // when
      ActivityAt result = ActivityAt.from(nullDateTime);

      // then
      assertThat(result).isNotNull();
      assertThat(result.toDate()).isEqualTo(LocalDate.MIN);
      assertThat(result.toDateTime().toLocalTime()).isEqualTo(LocalTime.MIN);
    }

    @Test
    @DisplayName("정상 LocalDateTime으로 생성하면 날짜와 시간이 모두 설정된다")
    void givenValidDateTime_whenFrom_thenReturnsActivityAtWithDateTime() {
      // given
      LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 14, 30, 0);

      // when
      ActivityAt result = ActivityAt.from(dateTime);

      // then
      assertThat(result.toDate()).isEqualTo(dateTime.toLocalDate());
      assertThat(result.toDateTime()).isEqualTo(dateTime);
    }
  }

  @Nested
  @DisplayName("변환 메서드")
  class ConversionMethods {

    @Test
    @DisplayName("startOfDay()는 날짜의 시작 시간(00:00:00)을 반환한다")
    void givenActivityAt_whenStartOfDay_thenReturnsStartOfDay() {
      // given
      LocalDate date = LocalDate.of(2024, 1, 15);
      ActivityAt activityAt = ActivityAt.of(date);

      // when
      LocalDateTime result = activityAt.startOfDay();

      // then
      assertThat(result).isEqualTo(LocalDateTime.of(2024, 1, 15, 0, 0, 0));
    }

    @Test
    @DisplayName("endOfDay()는 날짜의 끝 시간(23:59:59.999999999)을 반환한다")
    void givenActivityAt_whenEndOfDay_thenReturnsEndOfDay() {
      // given
      LocalDate date = LocalDate.of(2024, 1, 15);
      ActivityAt activityAt = ActivityAt.of(date);

      // when
      LocalDateTime result = activityAt.endOfDay();

      // then
      assertThat(result).isEqualTo(LocalDateTime.of(2024, 1, 15, 23, 59, 59, 999999999));
    }

    @Test
    @DisplayName("toDateTime()은 날짜와 시간을 LocalDateTime으로 변환한다")
    void givenActivityAt_whenToDateTime_thenReturnsLocalDateTime() {
      // given
      LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 14, 30, 0);
      ActivityAt activityAt = ActivityAt.from(dateTime);

      // when
      LocalDateTime result = activityAt.toDateTime();

      // then
      assertThat(result).isEqualTo(dateTime);
    }

    @Test
    @DisplayName("toDate()는 LocalDate를 반환한다")
    void givenActivityAt_whenToDate_thenReturnsLocalDate() {
      // given
      LocalDate date = LocalDate.of(2024, 1, 15);
      ActivityAt activityAt = ActivityAt.of(date);

      // when
      LocalDate result = activityAt.toDate();

      // then
      assertThat(result).isEqualTo(date);
    }
  }

  @Nested
  @DisplayName("datesUntil() 날짜 범위 생성")
  class DatesUntilMethod {

    @Test
    @DisplayName("EMPTY로 시작하면 예외가 발생한다")
    void givenEmptyStart_whenDatesUntil_thenThrowsException() {
      // given
      ActivityAt emptyStart = ActivityAt.of(null);
      ActivityAt end = ActivityAt.of(LocalDate.of(2024, 1, 15));

      // when & then
      assertThatThrownBy(() -> emptyStart.datesUntil(end))
          .isInstanceOf(ServerException.class)
          .hasFieldOrPropertyWithValue("baseErrorCode", ErrorCode.INVALID_FIELD_ERROR);
    }

    @Test
    @DisplayName("end가 null이면 시작 날짜만 포함하는 리스트를 반환한다")
    void givenNullEnd_whenDatesUntil_thenReturnsSingleDateList() {
      // given
      LocalDate date = LocalDate.of(2024, 1, 15);
      ActivityAt start = ActivityAt.of(date);

      // when
      List<LocalDate> result = start.datesUntil(null);

      // then
      assertThat(result).hasSize(1).containsExactly(date);
    }

    @Test
    @DisplayName("end가 EMPTY면 시작 날짜만 포함하는 리스트를 반환한다")
    void givenEmptyEnd_whenDatesUntil_thenReturnsSingleDateList() {
      // given
      LocalDate date = LocalDate.of(2024, 1, 15);
      ActivityAt start = ActivityAt.of(date);
      ActivityAt emptyEnd = ActivityAt.of(null);

      // when
      List<LocalDate> result = start.datesUntil(emptyEnd);

      // then
      assertThat(result).hasSize(1).containsExactly(date);
    }

    @Test
    @DisplayName("시작 날짜가 종료 날짜보다 이후면 예외가 발생한다")
    void givenStartAfterEnd_whenDatesUntil_thenThrowsException() {
      // given
      ActivityAt start = ActivityAt.of(LocalDate.of(2024, 1, 20));
      ActivityAt end = ActivityAt.of(LocalDate.of(2024, 1, 15));

      // when & then
      assertThatThrownBy(() -> start.datesUntil(end))
          .isInstanceOf(ServerException.class)
          .hasFieldOrPropertyWithValue("baseErrorCode", ErrorCode.INVALID_FIELD_ERROR);
    }

    @Test
    @DisplayName("시작과 종료가 같은 날이면 1개 요소 리스트를 반환한다")
    void givenSameStartAndEnd_whenDatesUntil_thenReturnsSingleDateList() {
      // given
      LocalDate date = LocalDate.of(2024, 1, 15);
      ActivityAt start = ActivityAt.of(date);
      ActivityAt end = ActivityAt.of(date);

      // when
      List<LocalDate> result = start.datesUntil(end);

      // then
      assertThat(result).hasSize(1).containsExactly(date);
    }

    @Test
    @DisplayName("정상 범위(7일)의 날짜 리스트를 반환한다")
    void givenValidRange_whenDatesUntil_thenReturnsDateList() {
      // given
      LocalDate startDate = LocalDate.of(2024, 1, 1);
      LocalDate endDate = LocalDate.of(2024, 1, 7);
      ActivityAt start = ActivityAt.of(startDate);
      ActivityAt end = ActivityAt.of(endDate);

      // when
      List<LocalDate> result = start.datesUntil(end);

      // then
      assertThat(result)
          .hasSize(7)
          .containsExactly(
              LocalDate.of(2024, 1, 1),
              LocalDate.of(2024, 1, 2),
              LocalDate.of(2024, 1, 3),
              LocalDate.of(2024, 1, 4),
              LocalDate.of(2024, 1, 5),
              LocalDate.of(2024, 1, 6),
              LocalDate.of(2024, 1, 7));
    }

    @Test
    @DisplayName("최대 범위(366일)의 날짜 리스트를 반환한다")
    void givenMaxRange_whenDatesUntil_thenReturnsDateList() {
      // given
      LocalDate startDate = LocalDate.of(2024, 1, 1);
      LocalDate endDate = LocalDate.of(2024, 12, 31);
      ActivityAt start = ActivityAt.of(startDate);
      ActivityAt end = ActivityAt.of(endDate);

      // when
      List<LocalDate> result = start.datesUntil(end);

      // then
      assertThat(result).hasSize(366);
      assertThat(result.get(0)).isEqualTo(startDate);
      assertThat(result.get(365)).isEqualTo(endDate);
    }

    @Test
    @DisplayName("범위가 366일을 초과하면 예외가 발생한다")
    void givenRangeExceedsMax_whenDatesUntil_thenThrowsException() {
      // given
      LocalDate startDate = LocalDate.of(2024, 1, 1);
      LocalDate endDate = LocalDate.of(2025, 1, 2); // 367 days
      ActivityAt start = ActivityAt.of(startDate);
      ActivityAt end = ActivityAt.of(endDate);

      // when & then
      assertThatThrownBy(() -> start.datesUntil(end))
          .isInstanceOf(ServerException.class)
          .hasFieldOrPropertyWithValue("baseErrorCode", ErrorCode.INVALID_FIELD_ERROR);
    }

    @Test
    @DisplayName("월 경계를 넘는 범위의 날짜 리스트를 정확하게 생성한다")
    void givenCrossMonthRange_whenDatesUntil_thenReturnsCorrectDateList() {
      // given
      LocalDate startDate = LocalDate.of(2024, 1, 30);
      LocalDate endDate = LocalDate.of(2024, 2, 2);
      ActivityAt start = ActivityAt.of(startDate);
      ActivityAt end = ActivityAt.of(endDate);

      // when
      List<LocalDate> result = start.datesUntil(end);

      // then
      assertThat(result)
          .hasSize(4)
          .containsExactly(
              LocalDate.of(2024, 1, 30),
              LocalDate.of(2024, 1, 31),
              LocalDate.of(2024, 2, 1),
              LocalDate.of(2024, 2, 2));
    }

    @Test
    @DisplayName("연 경계를 넘는 범위의 날짜 리스트를 정확하게 생성한다")
    void givenCrossYearRange_whenDatesUntil_thenReturnsCorrectDateList() {
      // given
      LocalDate startDate = LocalDate.of(2023, 12, 30);
      LocalDate endDate = LocalDate.of(2024, 1, 2);
      ActivityAt start = ActivityAt.of(startDate);
      ActivityAt end = ActivityAt.of(endDate);

      // when
      List<LocalDate> result = start.datesUntil(end);

      // then
      assertThat(result)
          .hasSize(4)
          .containsExactly(
              LocalDate.of(2023, 12, 30),
              LocalDate.of(2023, 12, 31),
              LocalDate.of(2024, 1, 1),
              LocalDate.of(2024, 1, 2));
    }
  }

  @Nested
  @DisplayName("equals() 및 hashCode()")
  class EqualsAndHashCode {

    @Test
    @DisplayName("같은 날짜와 시간을 가진 ActivityAt은 동일하다")
    void givenSameDateAndTime_whenEquals_thenReturnsTrue() {
      // given
      LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 14, 30, 0);
      ActivityAt activityAt1 = ActivityAt.from(dateTime);
      ActivityAt activityAt2 = ActivityAt.from(dateTime);

      // when & then
      assertThat(activityAt1).isEqualTo(activityAt2);
      assertThat(activityAt1.hashCode()).isEqualTo(activityAt2.hashCode());
    }

    @Test
    @DisplayName("다른 날짜를 가진 ActivityAt은 동일하지 않다")
    void givenDifferentDate_whenEquals_thenReturnsFalse() {
      // given
      ActivityAt activityAt1 = ActivityAt.of(LocalDate.of(2024, 1, 15));
      ActivityAt activityAt2 = ActivityAt.of(LocalDate.of(2024, 1, 16));

      // when & then
      assertThat(activityAt1).isNotEqualTo(activityAt2);
    }

    @Test
    @DisplayName("다른 시간을 가진 ActivityAt은 동일하지 않다")
    void givenDifferentTime_whenEquals_thenReturnsFalse() {
      // given
      ActivityAt activityAt1 = ActivityAt.from(LocalDateTime.of(2024, 1, 15, 14, 30, 0));
      ActivityAt activityAt2 = ActivityAt.from(LocalDateTime.of(2024, 1, 15, 15, 30, 0));

      // when & then
      assertThat(activityAt1).isNotEqualTo(activityAt2);
    }
  }
}
