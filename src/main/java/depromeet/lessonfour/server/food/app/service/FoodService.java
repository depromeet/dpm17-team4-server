package depromeet.lessonfour.server.food.app.service;

import java.util.List;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.food.app.dto.request.FoodSearchRequestDto;
import depromeet.lessonfour.server.food.app.dto.response.FoodSearchItem;
import depromeet.lessonfour.server.food.app.dto.response.FoodSearchResultDto;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FoodService {

  // private final FoodRepository foodRepository;

  public FoodSearchResultDto search(FoodSearchRequestDto request) {
    // 임시로 여러 개의 Food 데이터 생성
    List<FoodSearchItem> allFoods =
        List.of(
            new FoodSearchItem(1L, "김치찌개"),
            new FoodSearchItem(2L, "불고기"),
            new FoodSearchItem(3L, "비빔밥"),
            new FoodSearchItem(4L, "된장찌개"),
            new FoodSearchItem(5L, "갈비탕"),
            new FoodSearchItem(6L, "삼겹살"),
            new FoodSearchItem(7L, "냉면"),
            new FoodSearchItem(8L, "치킨"),
            new FoodSearchItem(9L, "피자"),
            new FoodSearchItem(10L, "라면"));

    Integer limit = request.count();
    List<FoodSearchItem> foodDtos = allFoods.stream().limit(limit).toList();

    return new FoodSearchResultDto(foodDtos);
  }
}
