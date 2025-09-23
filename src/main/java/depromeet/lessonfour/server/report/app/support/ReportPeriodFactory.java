package depromeet.lessonfour.server.report.app.support;

import depromeet.lessonfour.server.report.domain.vo.PeriodType;
import depromeet.lessonfour.server.report.domain.vo.ReportPeriod;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

public class ReportPeriodFactory {

  private static final int HOURS_IN_DAY = 23;
  private static final int MINUTES_IN_HOUR = 59;
  private static final int SECONDS_IN_MINUTE = 59;

  public static ReportPeriod create(LocalDateTime baseDateTime, PeriodType type) {
    return switch (type) {
      case DAILY -> createDaily(baseDateTime);
      case WEEKLY -> createWeekly(baseDateTime);
      case MONTHLY -> createMonthly(baseDateTime);
    };
  }

  private static ReportPeriod createDaily(LocalDateTime baseDateTime) {
    LocalDateTime start = baseDateTime.toLocalDate().atStartOfDay();
    LocalDateTime end = baseDateTime.toLocalDate().atTime(HOURS_IN_DAY, MINUTES_IN_HOUR, SECONDS_IN_MINUTE);
    return new ReportPeriod(start, end, PeriodType.DAILY);
  }

  private static ReportPeriod createWeekly(LocalDateTime baseDateTime) {
    LocalDate startOfWeek = baseDateTime.toLocalDate().with(DayOfWeek.MONDAY);
    LocalDate endOfWeek = baseDateTime.toLocalDate().with(DayOfWeek.SUNDAY);
    return new ReportPeriod(startOfWeek.atStartOfDay(), endOfWeek.atTime(HOURS_IN_DAY, MINUTES_IN_HOUR, SECONDS_IN_MINUTE), PeriodType.WEEKLY);
  }

  private static ReportPeriod createMonthly(LocalDateTime baseDateTime) {
    YearMonth month = YearMonth.from(baseDateTime);
    LocalDateTime start = month.atDay(1).atStartOfDay();
    LocalDateTime end = month.atEndOfMonth().atTime(HOURS_IN_DAY, MINUTES_IN_HOUR, SECONDS_IN_MINUTE);
    return new ReportPeriod(start, end, PeriodType.MONTHLY);
  }
}
