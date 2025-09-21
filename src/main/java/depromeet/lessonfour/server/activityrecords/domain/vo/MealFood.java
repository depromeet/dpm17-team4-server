package depromeet.lessonfour.server.activityrecords.domain.vo;

import depromeet.lessonfour.server.activityrecords.domain.entities.MealTime;
import depromeet.lessonfour.server.foods.domain.Food;

public record MealFood(MealTime mealTime, Food food) {}
