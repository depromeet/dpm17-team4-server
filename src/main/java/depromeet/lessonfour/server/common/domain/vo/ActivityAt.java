package depromeet.lessonfour.server.common.domain.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;

import depromeet.lessonfour.server.common.api.code.ErrorCode;
import depromeet.lessonfour.server.common.exception.ServerException;
import jakarta.annotation.Nullable;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Embeddable
@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class ActivityAt {

  private LocalDate date;
  private LocalTime time;

  private static final ActivityAt EMPTY = new ActivityAt(LocalDate.MIN, LocalTime.MIN);
  private static final int MAX_RANGE_DAYS = 366;

  public static ActivityAt of(LocalDate date) {
    if (date == null) {
      return EMPTY;
    }
    return new ActivityAt(date, LocalTime.MIDNIGHT); // 기본 시간을 자정으로 설정
  }

  public static ActivityAt from(LocalDateTime dateTime) {
    if (dateTime == null) {
      return EMPTY;
    }
    return new ActivityAt(dateTime.toLocalDate(), dateTime.toLocalTime());
  }

  public LocalDateTime startOfDay() {
    return date.atStartOfDay();
  }

  public LocalDateTime endOfDay() {
    return date.atTime(LocalTime.MAX);
  }

  public LocalDateTime toDateTime() {
    return LocalDateTime.of(date, time);
  }

  public LocalDate toDate() {
    return date;
  }

  public ActivityAt getLastMonth() {
    if (this.equals(EMPTY)) {
      throw new ServerException(ErrorCode.INVALID_FIELD_ERROR);
    }
    LocalDate lastMonthDate = date.minusMonths(1);
    return ActivityAt.from(lastMonthDate.atStartOfDay());
  }

  public ActivityAt getDayBefore() {
    if (this.equals(EMPTY)) {
      throw new ServerException(ErrorCode.INVALID_FIELD_ERROR);
    }
    LocalDate dayBeforeDate = date.minusDays(1);
    return ActivityAt.from(dayBeforeDate.atStartOfDay());
  }

  public ActivityAt getDayAfter() {
    if (this.equals(EMPTY)) {
      throw new ServerException(ErrorCode.INVALID_FIELD_ERROR);
    }
    LocalDate dayAfterDate = date.plusDays(1);
    return ActivityAt.from(dayAfterDate.atStartOfDay());
  }

  public List<LocalDate> datesUntil(@Nullable ActivityAt end) {
    if (this.equals(EMPTY)) {
      throw new ServerException(ErrorCode.INVALID_FIELD_ERROR);
    }
    if (end == null || end.equals(EMPTY)) {
      return List.of(date);
    }
    if (date.isAfter(end.date)) {
      throw new ServerException(ErrorCode.INVALID_FIELD_ERROR);
    }
    long days = ChronoUnit.DAYS.between(date, end.date) + 1;
    if (days > MAX_RANGE_DAYS) {
      throw new ServerException(ErrorCode.INVALID_FIELD_ERROR);
    }
    return Stream.iterate(date, d -> d.plusDays(1)).limit(days).toList();
  }

  public boolean isSameDate(@Nullable ActivityAt other) {
    if (other == null) {
      return false;
    }
    return this.date.equals(other.date);
  }

  public boolean isDayBefore(@Nullable ActivityAt other) {
    if (other == null || other.equals(EMPTY)) {
      return false;
    }
    return this.isSameDate(other.getDayBefore());
  }
}
