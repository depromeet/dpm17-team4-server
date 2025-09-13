package depromeet.lessonfour.server.foods.services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.foods.schemas.response.FoodDto;
import depromeet.lessonfour.server.foods.schemas.response.FoodSearchResultDto;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FoodService {

  // private final FoodRepository foodRepository;

  public FoodSearchResultDto search(String query) {
    // 임시로 5개의 Food 데이터 생성
    List<FoodDto> foodDtos =
        List.of(
            new FoodDto(UUID.randomUUID(), "김치찌개", 4.5),
            new FoodDto(UUID.randomUUID(), "불고기", 4.8),
            new FoodDto(UUID.randomUUID(), "비빔밥", 4.2),
            new FoodDto(UUID.randomUUID(), "된장찌개", 4.3),
            new FoodDto(UUID.randomUUID(), "갈비탕", 4.7));

    return new FoodSearchResultDto(foodDtos);
  }
}
