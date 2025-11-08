package depromeet.lessonfour.server.activityrecord.domain.entity;

import java.util.ArrayList;
import java.util.List;

import depromeet.lessonfour.server.activityrecord.domain.vo.MealFood;
import depromeet.lessonfour.server.activityrecord.domain.vo.StressLevel;
import depromeet.lessonfour.server.common.domain.entity.BaseTimeEntity;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
  private Long userId;

  // 마신 물 양 (종이컵 기준, 0~10개)
  @Column
  @Min(0)
  @Max(10)
  private Integer waterIntakeCups;

  // 스트레스 레벨 (5단계 Enum)
  @Column
  @Enumerated(EnumType.STRING)
  private StressLevel stressLevel;

  @Column(nullable = false)
  @NotNull @AttributeOverrides({
    @AttributeOverride(name = "date", column = @Column(name = "activity_date")),
    @AttributeOverride(name = "time", column = @Column(name = "activity_time"))
  })
  private ActivityAt activityAt;

  @Column(nullable = false)
  @NotNull private boolean isDeleted;

  @OneToMany(
      mappedBy = "activityRecord",
      fetch = FetchType.LAZY,
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  @Builder.Default
  private List<FoodRecord> foodRecords = new ArrayList<>();

  public static ActivityRecord createWithMeals(
      Long userId,
      Integer waterIntakeCups,
      StressLevel stressLevel,
      ActivityAt activityAt,
      List<MealFood> mealFoods) {
    ActivityRecord activityRecord =
        ActivityRecord.builder()
            .userId(userId)
            .waterIntakeCups(waterIntakeCups)
            .stressLevel(stressLevel)
            .activityAt(activityAt)
            .build();

    if (mealFoods != null) {
      mealFoods.stream()
          .map(
              mealFood ->
                  FoodRecord.createRecord(activityRecord, mealFood.food(), mealFood.mealTime()))
          .forEach(activityRecord.foodRecords::add);
    }

    return activityRecord;
  }

  public void delete() {
    this.isDeleted = true;
  }

  public boolean isOwnedBy(Long userId) {
    return this.userId.equals(userId);
  }

  public void update(Integer water, StressLevel stress, List<MealFood> mealFoods) {
    if (water != null) this.waterIntakeCups = water;
    if (stress != null) this.stressLevel = stress;
    if (mealFoods != null) {
      this.foodRecords.clear();
      mealFoods.stream()
          .map(mealFood -> FoodRecord.createRecord(this, mealFood.food(), mealFood.mealTime()))
          .forEach(this.foodRecords::add);
    }
  }
}
