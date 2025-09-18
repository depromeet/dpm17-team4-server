package depromeet.lessonfour.server.foods.services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.foods.schemas.request.FoodSearchRequestDto;
import depromeet.lessonfour.server.foods.schemas.response.FoodSearchItem;
import depromeet.lessonfour.server.foods.schemas.response.FoodSearchResultDto;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FoodService {

  // private final FoodRepository foodRepository;

  public FoodSearchResultDto search(FoodSearchRequestDto request) {
    // 임시로 여러 개의 Food 데이터 생성
    List<FoodSearchItem> allFoods =
        List.of(
            new FoodSearchItem(UUID.randomUUID(), "김치찌개"),
            new FoodSearchItem(UUID.randomUUID(), "불고기"),
            new FoodSearchItem(UUID.randomUUID(), "비빔밥"),
            new FoodSearchItem(UUID.randomUUID(), "된장찌개"),
            new FoodSearchItem(UUID.randomUUID(), "갈비탕"),
            new FoodSearchItem(UUID.randomUUID(), "삼겹살"),
            new FoodSearchItem(UUID.randomUUID(), "냉면"),
            new FoodSearchItem(UUID.randomUUID(), "치킨"),
            new FoodSearchItem(UUID.randomUUID(), "피자"),
            new FoodSearchItem(UUID.randomUUID(), "라면"));

    Integer limit = Math.min(Math.max(request.count(), 1), 100);
    List<FoodSearchItem> foodDtos = allFoods.stream().limit(limit).toList();

    return new FoodSearchResultDto(foodDtos);
  }
}
