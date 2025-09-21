package depromeet.lessonfour.server.food.app.service;

import java.util.List;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.food.domain.entity.Food;
import depromeet.lessonfour.server.food.infra.repository.FoodRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FoodQueryService {

  private final FoodRepository foodRepository;

  public List<Food> findByIds(List<Long> foodIds) {
    return foodRepository.findAllByIdIn(foodIds);
  }
}
