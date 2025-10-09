package depromeet.lessonfour.server.common.domain.view;

import java.time.LocalDate;

public interface DailyExistenceView {
  LocalDate date();

  boolean exists();
}
