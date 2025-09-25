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
@Builder(access = AccessLevel.PRIVATE)
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

  // 정적 팩토리 메서드: name과 score만으로 생성 (usage_count는 기본값 0)
  public static Food of(String name, double score) {
    return Food.builder().name(name).score(score).usage_count(0L).build();
  }

  // 정적 팩토리 메서드: name, score, usage_count 모두 지정
  public static Food of(String name, double score, Long usageCount) {
    return Food.builder().name(name).score(score).usage_count(usageCount).build();
  }
}
