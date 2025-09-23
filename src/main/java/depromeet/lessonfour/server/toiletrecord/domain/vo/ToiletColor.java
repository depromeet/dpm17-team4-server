package depromeet.lessonfour.server.toiletrecord.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ToiletColor {
  DEFAULT,
  GOLD,
  DARK_BROWN,
  RED,
  GREEN,
  GRAY;

  public int getScore() {
    return switch (this) {
      case DEFAULT, GOLD -> 0;
      case DARK_BROWN -> -5;
      case RED, GREEN, GRAY -> -20; // 비정상
    };
  }
}
