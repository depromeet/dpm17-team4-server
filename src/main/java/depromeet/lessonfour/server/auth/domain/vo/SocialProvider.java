package depromeet.lessonfour.server.auth.domain.vo;

import depromeet.lessonfour.server.user.domain.vo.Provider;
import lombok.Getter;

@Getter
public enum SocialProvider {
  KAKAO("kakao"),
  APPLE("apple");

  private final String value;

  SocialProvider(String value) {
    this.value = value;
  }

  public static SocialProvider from(String value) {
    for (SocialProvider type : values()) {
      if (type.name().equalsIgnoreCase(value)) return type;
    }
    throw new IllegalArgumentException("Unknown provider: " + value);
  }

  public static SocialProvider from(Provider provider) {
    return switch (provider.getType()) {
      case KAKAO -> SocialProvider.KAKAO;
      case APPLE -> SocialProvider.APPLE;
      case LOCAL -> null;
    };
  }
}
