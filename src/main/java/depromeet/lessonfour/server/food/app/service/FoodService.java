package depromeet.lessonfour.server.food.app.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.food.app.dto.request.FoodSearchRequestDto;
import depromeet.lessonfour.server.food.app.dto.response.FoodSearchItem;
import depromeet.lessonfour.server.food.app.dto.response.FoodSearchResultDto;
import depromeet.lessonfour.server.food.domain.entity.Food;
import depromeet.lessonfour.server.food.infra.repository.FoodRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FoodService {

  private final FoodRepository foodRepository;

  public FoodSearchResultDto search(FoodSearchRequestDto request) {
    List<Food> foods = foodRepository.findByNameContaining(request.query(), request.count());
    List<FoodSearchItem> searchResult =
        foods.stream().map(food -> new FoodSearchItem(food.getId(), food.getName())).toList();
    return new FoodSearchResultDto(searchResult);
  }

  @Transactional
  public void incrementUsageCount(Long foodId) {
    foodRepository.incrementUsageCount(foodId);
  }
}
