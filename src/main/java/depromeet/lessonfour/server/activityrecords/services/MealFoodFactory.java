package depromeet.lessonfour.server.activityrecords.services;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.activityrecords.domain.vo.MealFood;
import depromeet.lessonfour.server.activityrecords.schemas.request.CreateActivityRecordsRequest.CreateFoodRequestDto;
import depromeet.lessonfour.server.common.exception.ServerException;
import depromeet.lessonfour.server.common.exception.code.ErrorCode;
import depromeet.lessonfour.server.foods.domain.Food;
import depromeet.lessonfour.server.foods.services.FoodQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MealFoodFactory {

  private final FoodQueryService foodQueryService;

  public List<MealFood> createMealFoods(List<CreateFoodRequestDto> dtos) {
    if (dtos == null || dtos.isEmpty()) {
      return List.of();
    }
    Map<Long, Food> foodMap = getFoodMap(dtos);

    return dtos.stream()
        .map(
            dto -> {
              Food food = foodMap.get(dto.id());
              if (food == null) {
                throw new ServerException(ErrorCode.INTERNAL_SERVER_ERROR);
              }
              return new MealFood(dto.mealTime(), food);
            })
        .toList();
  }

  private Map<Long, Food> getFoodMap(List<CreateFoodRequestDto> dtos) {
    List<Long> foodIds = dtos.stream().map(CreateFoodRequestDto::id).toList();

    return foodQueryService.findByIds(foodIds).stream()
        .collect(Collectors.toMap(Food::getId, food -> food));
  }
}
