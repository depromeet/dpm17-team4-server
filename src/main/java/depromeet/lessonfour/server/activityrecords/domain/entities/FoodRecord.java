package depromeet.lessonfour.server.activityrecords.domain.entities;

import java.util.UUID;

import depromeet.lessonfour.server.common.entities.BaseTimeEntity;
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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "food_record")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FoodRecord extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "activity_record_id", nullable = false)
	private ActivityRecord activityRecord;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "food_id", nullable = false)
	private Food food;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MealTime mealTime;

	public static FoodRecord register(ActivityRecord activityRecord, Food food, MealTime mealTime) {
		return FoodRecord.builder()
			.activityRecord(activityRecord)
			.food(food)
			.mealTime(mealTime)
			.build();
	}
}
