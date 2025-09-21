package depromeet.lessonfour.server.food.app.service;

import java.util.List;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.common.api.code.ErrorCode;
import depromeet.lessonfour.server.common.exception.ServerException;
import depromeet.lessonfour.server.food.domain.entity.Food;
import depromeet.lessonfour.server.food.infra.repository.FoodRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FoodQueryService {

  private final FoodRepository foodRepository;

  public List<Food> findByIds(List<Long> foodIds) {
    List<Food> foods = foodRepository.findAllByIdIn(foodIds);
    if (foods.size() != new java.util.HashSet<>(foodIds).size()) {
      throw new ServerException(ErrorCode.DATA_NOT_FOUND);
    }
    return foods;
  }
}
