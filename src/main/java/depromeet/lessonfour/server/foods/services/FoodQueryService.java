package depromeet.lessonfour.server.foods.services;

import java.util.List;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.foods.adapters.FoodRepository;
import depromeet.lessonfour.server.foods.domain.Food;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FoodQueryService {

  private final FoodRepository foodRepository;

  public List<Food> findByIds(List<Long> foodIds) {
    return foodRepository.findAllByIdIn(foodIds);
  }
}
