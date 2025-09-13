package depromeet.lessonfour.server.toiletrecords.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ToiletStatus {
  SUCCESS, // 쌌어요
  FAIL, // 못쌌어요
  ATTEMPT // 시도만 했어요
}
