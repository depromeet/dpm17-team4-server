package depromeet.lessonfour.server.activityrecords.domain.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import depromeet.lessonfour.server.activityrecords.domain.vo.MealFood;
import depromeet.lessonfour.server.common.entities.BaseTimeEntity;
import depromeet.lessonfour.server.users.domain.entities.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// TODO : 추후 기능 명세에 따라 notnull 추가
@Entity
@Table(name = "activity_record")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActivityRecord extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 작성자
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  // 마신 물 양 (종이컵 기준, 0~10개)
  @Column
  @Min(0)
  @Max(10)
  private int waterIntakeCups;

  // 스트레스 레벨 (5단계 Enum)
  @Column
  @Enumerated(EnumType.STRING)
  private StressLevel stressLevel;

  @Column(nullable = false)
  @NotNull private LocalDateTime activityAt;

  @Column(nullable = false)
  @NotNull private boolean isDeleted;

  @OneToMany(
      mappedBy = "activityRecord",
      fetch = FetchType.LAZY,
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  @Builder.Default
  private final List<FoodRecord> foodRecords = new ArrayList<>();

  public static ActivityRecord createWithMeals(
      User user,
      int waterIntakeCups,
      StressLevel stressLevel,
      LocalDateTime activityAt,
      List<MealFood> mealFoods) {
    ActivityRecord activityRecord =
        ActivityRecord.builder()
            .user(user)
            .waterIntakeCups(waterIntakeCups)
            .stressLevel(stressLevel)
            .activityAt(activityAt)
            .build();

    mealFoods.stream()
        .map(
            mealFood ->
                FoodRecord.createRecord(activityRecord, mealFood.food(), mealFood.mealTime()))
        .forEach(activityRecord.foodRecords::add);

    return activityRecord;
  }
}
