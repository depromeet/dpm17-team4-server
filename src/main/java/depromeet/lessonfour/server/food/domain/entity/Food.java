package depromeet.lessonfour.server.food.domain.entity;

import depromeet.lessonfour.server.common.domain.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "foods")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Food extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  @NotNull private String name;

  @Column(name = "score", nullable = false)
  @NotNull private double score;

  @Column(nullable = false)
  @NotNull @Builder.Default
  private Long usage_count = 0L;

  // 테스트용 생성 메서드
  public static Food createForTest(String name, double score) {
    return Food.builder().name(name).score(score).build();
  }

  public boolean isDangerous(int threshold) {
    return this.score >= threshold;
  }
}
