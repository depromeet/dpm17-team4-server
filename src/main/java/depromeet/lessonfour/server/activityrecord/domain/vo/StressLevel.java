package depromeet.lessonfour.server.activityrecord.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StressLevel {
  VERY_LOW(0, 20),
  LOW(21, 40),
  MEDIUM(41, 60),
  HIGH(61, 80),
  VERY_HIGH(81, 100);

  private final int min;
  private final int max;
}
