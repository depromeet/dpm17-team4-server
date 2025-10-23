package depromeet.lessonfour.server.user.domain.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Gender {
  M("M"),
  F("F");

  private final String value;

  public static Gender from(String value) {
    if (value == null) {
      return null;
    }
    for (Gender gender : Gender.values()) {
      if (gender.value.equals(value)) {
        return gender;
      }
    }
    throw new IllegalArgumentException("Invalid gender value: " + value);
  }
}
