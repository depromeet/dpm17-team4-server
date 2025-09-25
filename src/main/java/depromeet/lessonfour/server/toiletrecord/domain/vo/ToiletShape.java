package depromeet.lessonfour.server.toiletrecord.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ToiletShape {
  RABBIT,
  ROCK,
  CORN,
  BANANA,
  CREAM,
  PORRIDGE;

  public int getScore() {
    return switch (this) {
      case RABBIT, ROCK -> -15;
      case PORRIDGE -> -10; // 묽음
      case CORN -> -5;
      case CREAM -> 10;
      case BANANA -> 15; // 이상적
    };
  }
}
