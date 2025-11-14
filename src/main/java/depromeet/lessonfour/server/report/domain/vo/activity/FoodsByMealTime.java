package depromeet.lessonfour.server.report.domain.vo.activity;

import java.time.LocalDate;
import java.util.List;

import depromeet.lessonfour.server.activityrecord.domain.vo.MealTime;

public record FoodsByMealTime(LocalDate occurredAt, MealTime mealTime, List<String> foods) {}
