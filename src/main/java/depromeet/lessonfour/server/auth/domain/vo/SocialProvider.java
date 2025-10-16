package depromeet.lessonfour.server.auth.domain.vo;

import depromeet.lessonfour.server.auth.api.code.AuthErrorCode;
import depromeet.lessonfour.server.common.exception.ServerException;
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
    throw new ServerException(AuthErrorCode.OAUTH_PROVIDER_NOT_FOUND);
  }

  public static SocialProvider from(Provider provider) {
    return switch (provider.getType()) {
      case KAKAO -> SocialProvider.KAKAO;
      case APPLE -> SocialProvider.APPLE;
      case LOCAL -> null;
    };
  }

  public Provider toProvider(String providerId) {
    Provider.ProviderType providerType =
        switch (this) {
          case KAKAO -> Provider.ProviderType.KAKAO;
          case APPLE -> Provider.ProviderType.APPLE;
        };
    return Provider.of(providerType, providerId);
  }
}
