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
  WHITE("흰색"),
  BLACK("흑색"),
  NONE("실패");

  private final String value;

  public int getScore() {
    return switch (this) {
      case DEFAULT, GOLD -> 3;
      case DARK_BROWN -> -2;
      case RED, GREEN, WHITE, BLACK -> -12; // 비정상
      case NONE -> 0; // 실패한 경우 점수에 영향 없음
    };
  }
}
