package depromeet.lessonfour.server.toiletrecord.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ToiletShape {
  RABBIT("토끼"),
  CORN("옥수수"),
  BANANA("바나나"),
  CREAM("크림"),
  PORRIDGE("죽"),
  WATER("물");

  public final String value;

  public int getScore() {
    return switch (this) {
      case RABBIT -> -15;
      case PORRIDGE -> -10; // 묽음
      case CORN -> -5;
      case CREAM, WATER -> 10;
      case BANANA -> 15; // 이상적
    };
  }
}
