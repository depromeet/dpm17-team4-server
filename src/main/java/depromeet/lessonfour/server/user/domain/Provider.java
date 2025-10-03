package depromeet.lessonfour.server.user.domain;

import java.util.Objects;

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

  public enum ProviderType {
    KAKAO,
    APPLE,
    LOCAL,
    // GOOGLE
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
}
