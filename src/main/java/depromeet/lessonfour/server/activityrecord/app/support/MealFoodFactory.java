package depromeet.lessonfour.server.activityrecord.app.support;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.activityrecord.app.dto.request.ActivityRecordDto.FoodDto;
import depromeet.lessonfour.server.activityrecord.domain.vo.MealFood;
import depromeet.lessonfour.server.common.api.code.ErrorCode;
import depromeet.lessonfour.server.common.exception.ServerException;
import depromeet.lessonfour.server.food.app.service.FoodQueryService;
import depromeet.lessonfour.server.food.domain.entity.Food;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MealFoodFactory {

  private final FoodQueryService foodQueryService;

  public List<MealFood> createMealFoods(List<FoodDto> dtos) {
    if (dtos == null) {
      return null;
    }
    if (dtos.isEmpty()) {
      return List.of();
    }
    Map<Long, Food> foodMap = getFoodMap(dtos);

    return dtos.stream()
        .map(
            dto -> {
              Food food = foodMap.get(dto.id());
              if (food == null) {
                throw new ServerException(ErrorCode.DATA_NOT_FOUND);
              }
              return new MealFood(dto.mealTime(), food);
            })
        .toList();
  }

  private Map<Long, Food> getFoodMap(List<FoodDto> dtos) {
    List<Long> foodIds = dtos.stream().map(FoodDto::id).distinct().toList();

    return foodQueryService.findByIds(foodIds).stream()
        .collect(Collectors.toMap(Food::getId, food -> food, (a, b) -> a));
  }
}
