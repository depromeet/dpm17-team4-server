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

  public FoodSearchResultDto search(String query, int count) {
    // 임시로 여러 개의 Food 데이터 생성
    List<FoodDto> allFoods = List.of(
        new FoodDto(UUID.randomUUID(), "김치찌개", 4.5),
        new FoodDto(UUID.randomUUID(), "불고기", 4.8),
        new FoodDto(UUID.randomUUID(), "비빔밥", 4.2),
        new FoodDto(UUID.randomUUID(), "된장찌개", 4.3),
        new FoodDto(UUID.randomUUID(), "갈비탕", 4.7),
        new FoodDto(UUID.randomUUID(), "삼겹살", 4.6),
        new FoodDto(UUID.randomUUID(), "냉면", 4.1),
        new FoodDto(UUID.randomUUID(), "치킨", 4.9),
        new FoodDto(UUID.randomUUID(), "피자", 4.4),
        new FoodDto(UUID.randomUUID(), "라면", 3.8)
    );

    // count만큼만 반환 (최대 10개)
    int limit = Math.min(Math.max(count, 1), 10); // 1~10 사이로 제한
    List<FoodDto> foodDtos = allFoods.stream()
        .limit(limit)
        .toList();

    return new FoodSearchResultDto(foodDtos);
  }
}
