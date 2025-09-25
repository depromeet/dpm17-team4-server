package depromeet.lessonfour.server.report.domain.vo;

import lombok.Getter;

@Getter
public enum WaterLevel {
  STANDARD("#4E5560"),
  HIGH("#23ABFF"),
  MEDIUM("#F4B005"),
  LOW("#F13A49");

  private final String color;

  WaterLevel(String color) {
    this.color = color;
  }
}
