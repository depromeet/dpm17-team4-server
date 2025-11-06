package depromeet.lessonfour.server.report.domain.vo.monthly;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DayPeriod {
  MORNING("오전"),
  AFTERNOON("오후"),
  EVENING("저녁");

  private final String value;
}
