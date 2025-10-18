package depromeet.lessonfour.server.common.domain.vo;

import java.time.LocalDate;

public interface DailyExistence {
  LocalDate date();

  boolean exists();
}
