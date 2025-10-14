package depromeet.lessonfour.server.common.domain.view;

import java.time.LocalTime;

public interface RecordTimeView {
  Long id();

  LocalTime activityTime();
}
