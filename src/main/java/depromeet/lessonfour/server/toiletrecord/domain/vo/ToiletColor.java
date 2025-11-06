package depromeet.lessonfour.server.toiletrecord.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ToiletColor {
  DEFAULT("갈색"),
  GOLD("황금색"),
  DARK_BROWN("갈색"),
  RED("적색"),
  GREEN("녹색"),
  GRAY("흰색"),
  BLACK("흑색");

  private final String value;

  public int getScore() {
    return switch (this) {
      case DEFAULT, GOLD -> 0;
      case DARK_BROWN -> -5;
      case RED, GREEN, GRAY, BLACK -> -20; // 비정상
    };
  }
}
