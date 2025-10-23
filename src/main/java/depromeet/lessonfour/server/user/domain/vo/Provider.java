package depromeet.lessonfour.server.user.domain.vo;

import java.util.Objects;

import depromeet.lessonfour.server.auth.api.code.AuthErrorCode;
import depromeet.lessonfour.server.common.exception.ServerException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Embeddable
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
public final class Provider {

  @Getter
  public enum ProviderType {
    KAKAO("kakao"),
    APPLE("apple"),
    LOCAL("local");

    private final String value;

    ProviderType(String value) {
      this.value = value;
    }

    public static ProviderType from(String value) {
      for (ProviderType type : values()) {
        if (type.name().equalsIgnoreCase(value)) {
          return type;
        }
      }
      throw new ServerException(AuthErrorCode.OAUTH_PROVIDER_NOT_FOUND);
    }
  }

  @Enumerated(EnumType.STRING)
  @Column(name = "provider_type", nullable = false)
  private ProviderType type;

  @Column(name = "provider_id")
  private String id;

  private Provider(ProviderType type, String id) {
    this.type = Objects.requireNonNull(type, "type must not be null");
    if (type != ProviderType.LOCAL && (id == null || id.isBlank())) {
      throw new IllegalArgumentException("id must not be null or blank");
    }
    this.id = id;
  }

  public static Provider of(ProviderType type, String id) {
    return new Provider(type, id);
  }

  public static Provider local() {
    return of(ProviderType.LOCAL, null);
  }

  public static Provider kakao(String providerId) {
    return of(ProviderType.KAKAO, providerId);
  }

  public static Provider apple(String providerId) {
    return of(ProviderType.APPLE, providerId);
  }
}
