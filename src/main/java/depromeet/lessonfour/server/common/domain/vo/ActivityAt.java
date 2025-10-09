package depromeet.lessonfour.server.common.domain.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class ActivityAt {

  private LocalDate date;
  private LocalTime time;

  public static ActivityAt of(LocalDate date) {
    return new ActivityAt(date, LocalTime.MIDNIGHT); // 기본 시간을 자정으로 설정
  }

  public static ActivityAt from(LocalDateTime dateTime) {
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
}
