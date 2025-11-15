package depromeet.lessonfour.server.common.domain.vo;

import java.time.DayOfWeek;
import java.time.LocalDate;

public record WeekRange(LocalDate start, LocalDate endExclusive) {

  /** 이번 주의 WeekRange를 반환합니다. (월요일 시작, 다음주 월요일 종료) */
  public static WeekRange of(LocalDate baseDate) {
    LocalDate startOfWeek = baseDate.with(DayOfWeek.MONDAY);
    LocalDate endOfWeek = startOfWeek.plusDays(7);

    return new WeekRange(startOfWeek, endOfWeek);
  }

  /** 이전 주의 WeekRange를 반환합니다. */
  public WeekRange previous() {
    LocalDate startPrev = start.minusWeeks(1);
    return new WeekRange(startPrev, startPrev.plusDays(7));
  }

  /** start를 ActivityAt으로 변환합니다. */
  public ActivityAt startAsActivityAt() {
    return ActivityAt.of(start);
  }

  /** endExclusive를 ActivityAt으로 변환합니다. */
  public ActivityAt endAsActivityAt() {
    return ActivityAt.of(endExclusive);
  }
}
