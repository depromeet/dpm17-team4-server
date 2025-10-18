package depromeet.lessonfour.server.common.domain.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import depromeet.lessonfour.server.common.api.code.ErrorCode;
import depromeet.lessonfour.server.common.exception.ServerException;
import jakarta.annotation.Nullable;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class ActivityAt {

  private LocalDate date;
  private LocalTime time;

  private static final ActivityAt EMPTY = new ActivityAt(LocalDate.MIN, LocalTime.MIN);

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

  public List<LocalDate> datesUntil(@Nullable ActivityAt end) {
    if (end == null || end.equals(EMPTY)) {
      return List.of(date);
    }

    if (date.isAfter(end.date)) {
      throw new ServerException(ErrorCode.INVALID_FIELD_ERROR);
    }

    return date.datesUntil(end.date.plusDays(1)).toList();
  }
}
