package depromeet.lessonfour.server.food.domain.entity;

import java.util.ArrayList;
import java.util.List;

import depromeet.lessonfour.server.common.domain.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tags")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  @NotNull private String name;

  @Column private String category;

  @Column private String description;

  @OneToMany(mappedBy = "tag")
  @Builder.Default
  private List<FoodTag> foodTags = new ArrayList<>();

  public static Tag createForTest(String name, String category) {
    return Tag.builder().name(name).category(category).build();
  }
}
