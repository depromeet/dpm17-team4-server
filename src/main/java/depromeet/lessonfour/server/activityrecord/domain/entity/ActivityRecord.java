package depromeet.lessonfour.server.activityrecord.domain.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import depromeet.lessonfour.server.activityrecord.domain.vo.MealFood;
import depromeet.lessonfour.server.activityrecord.domain.vo.StressLevel;
import depromeet.lessonfour.server.common.api.code.ErrorCode;
import depromeet.lessonfour.server.common.domain.entity.BaseTimeEntity;
import depromeet.lessonfour.server.common.exception.ServerException;
import depromeet.lessonfour.server.report.domain.vo.FoodEvaluation;
import depromeet.lessonfour.server.report.domain.vo.StressEvaluation;
import depromeet.lessonfour.server.report.domain.vo.WaterEvaluation;
import depromeet.lessonfour.server.report.domain.vo.WaterLevel;
import depromeet.lessonfour.server.user.domain.entity.User;
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
import java.time.LocalDateTime;
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
  private List<FoodRecord> foodRecords = new ArrayList<>();

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

  public void delete() {
    this.isDeleted = true;
  }

  public WaterEvaluation evaluateWater() {
    if (waterIntakeCups >= 8) {
      return new WaterEvaluation(waterIntakeCups, WaterLevel.HIGH);
    } else if (waterIntakeCups >= 5) {
      return new WaterEvaluation(waterIntakeCups, WaterLevel.MEDIUM);
    } else {
      return new WaterEvaluation(waterIntakeCups, WaterLevel.LOW);
    }
  }

  public StressEvaluation evaluateStress() {
    return switch (stressLevel) {
      case VERY_LOW -> StressEvaluation.VERY_LOW;
      case LOW      -> StressEvaluation.LOW;
      case MEDIUM   -> StressEvaluation.MEDIUM;
      case HIGH     -> StressEvaluation.HIGH;
      case VERY_HIGH-> StressEvaluation.VERY_HIGH;
      default -> throw new ServerException(ErrorCode.INTERNAL_SERVER_ERROR);
    };
  }

  public FoodEvaluation evaluateFood() {

  }
}
