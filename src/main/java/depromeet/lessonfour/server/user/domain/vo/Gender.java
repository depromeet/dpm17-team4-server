package depromeet.lessonfour.server.user.domain.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Gender {
  M("M"),
  F("F");

  private final String value;

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static Gender from(String value) {
    if (value == null) {
      return null;
    }
    for (Gender gender : Gender.values()) {
      if (gender.value.equals(value)) {
        return gender;
      }
    }
    throw new IllegalArgumentException("성별은 \"M\" 또는 \"F\" 중 하나여야 합니다. 입력된 값: " + value);
  }
}
