package depromeet.lessonfour.server.toiletrecords.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ToiletDuration {
  WITHIN_FIVE_MINUTES,
  WITHIN_TEN_MINUTES,
  OVER_TEN_MINUTES
}
